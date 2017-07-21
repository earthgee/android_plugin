package cn.jiajixin.nuwa

import cn.jiajixin.nuwa.util.*
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

class NuwaPlugin implements Plugin<Project> {
    //需要注入的包
    HashSet<String> includePackage
    //排除注入的类
    HashSet<String> excludeClass
    //debug模式下是否开启nuwa
    def debugOn
    def patchList = []
    def beforeDexTasks = []
    //patch时会用到的参数
    private static final String NUWA_DIR = "NuwaDir"
    private static final String NUWA_PATCHES = "nuwaPatches"

    //混淆文件 
    private static final String MAPPING_TXT = "mapping.txt"
    //类hash文件
    private static final String HASH_TXT = "hash.txt"

    private static final String DEBUG = "debug"


    @Override
    void apply(Project project) {

        //使用此插件的app可以通过nuwa修改配置
        project.extensions.create("nuwa", NuwaExtension, project)

        //项目的任务有向图构建之后
        project.afterEvaluate {
            def extension = project.extensions.findByName("nuwa") as NuwaExtension
            //可配置的一些参数
            includePackage = extension.includePackage
            excludeClass = extension.excludeClass
            debugOn = extension.debugOn

            //对于每一个变体
            project.android.applicationVariants.each { variant ->
                //选择使用nuwa
                if (!variant.name.contains(DEBUG) || (variant.name.contains(DEBUG) && debugOn)) {

                    Map hashMap
                    File nuwaDir
                    File patchDir

                    //没开启multidex会有此任务
                    def preDexTask = project.tasks.findByName("preDex${variant.name.capitalize()}")
                    //dex任务
                    def dexTask = project.tasks.findByName("dex${variant.name.capitalize()}")
                    //混淆任务
                    def proguardTask = project.tasks.findByName("proguard${variant.name.capitalize()}")

                    def processManifestTask = project.tasks.findByName("process${variant.name.capitalize()}Manifest")
                    def manifestFile = processManifestTask.outputs.files.files[0]

                    //正常打包得到的nuwa目录 对应nuwa官方指导的第一步
                    def oldNuwaDir = NuwaFileUtils.getFileFromProperty(project, NUWA_DIR)
                    if (oldNuwaDir) {
                        //拿到map文件 应用到proguard任务中
                        def mappingFile = NuwaFileUtils.getVariantFile(oldNuwaDir, variant, MAPPING_TXT)
                        //应用到proguard上
                        NuwaAndroidUtils.applymapping(proguardTask, mappingFile)
                    }
                    if (oldNuwaDir) {
                        //拿到hash文件
                        def hashFile = NuwaFileUtils.getVariantFile(oldNuwaDir, variant, HASH_TXT)
                        //存到hashmap里
                        hashMap = NuwaMapUtils.parseMap(hashFile)
                    }

                    //mapping和hash保存的位置
                    def dirName = variant.dirName
                    nuwaDir = new File("${project.buildDir}/outputs/nuwa")
                    def outputDir = new File("${nuwaDir}/${dirName}")
                    def hashFile = new File(outputDir, "hash.txt")


                    //准备闭包
                    Closure nuwaPrepareClosure = {
                        //得到application类名
                        def applicationName = NuwaAndroidUtils.getApplication(manifestFile)
                        //将application类加入到excludeClass中
                        if (applicationName != null) {
                            excludeClass.add(applicationName)
                        }

                        //example build/outputs/nuwa/qihoo
                        outputDir.mkdirs()
                        if (!hashFile.exists()) {
                            hashFile.createNewFile()
                        }

                        //创建patch目录
                        if (oldNuwaDir) {
                            patchDir = new File("${nuwaDir}/${dirName}/patch")
                            patchDir.mkdirs()
                            patchList.add(patchDir)
                        }
                    }

                    //example nuwaQihooDebugPatch 第三步要用到的task
                    def nuwaPatch = "nuwa${variant.name.capitalize()}Patch"
                    project.task(nuwaPatch) << {
                        if (patchDir) {
                            NuwaAndroidUtils.dex(project, patchDir)
                        }
                    }
                    def nuwaPatchTask = project.tasks[nuwaPatch]

                    Closure copyMappingClosure = {
                        if (proguardTask) {
                            //copy mapping文件
                            def mapFile = new File("${project.buildDir}/outputs/mapping/${variant.dirName}/mapping.txt")
                            def newMapFile = new File("${nuwaDir}/${variant.dirName}/mapping.txt");
                            FileUtils.copyFile(mapFile, newMapFile)
                        }
                    }

                    if (preDexTask) {
                        //没有开启multidex  nuwaJarBeforePreDex-->preDex-->nuwaClassBeforeDex-->dex?nuwaPatch?
                        def nuwaJarBeforePreDex = "nuwaJarBeforePreDex${variant.name.capitalize()}"
                        project.task(nuwaJarBeforePreDex) << {
                            //这些文件是所有的库工程和第三方jar包
                            Set<File> inputFiles = preDexTask.inputs.files.files
                            inputFiles.each { inputFile ->
                                def path = inputFile.absolutePath
                                if (NuwaProcessor.shouldProcessPreDexJar(path)) {
                                    //注入字节码
                                    NuwaProcessor.processJar(hashFile, inputFile, patchDir, hashMap, includePackage, excludeClass)
                                }
                            }
                        }
                        def nuwaJarBeforePreDexTask = project.tasks[nuwaJarBeforePreDex]
                        nuwaJarBeforePreDexTask.dependsOn preDexTask.taskDependencies.getDependencies(preDexTask)
                        preDexTask.dependsOn nuwaJarBeforePreDexTask

                        nuwaJarBeforePreDexTask.doFirst(nuwaPrepareClosure)

                        //处理classes文件，是主工程的文件
                        def nuwaClassBeforeDex = "nuwaClassBeforeDex${variant.name.capitalize()}"
                        project.task(nuwaClassBeforeDex) << {
                            Set<File> inputFiles = dexTask.inputs.files.files
                            inputFiles.each { inputFile ->
                                def path = inputFile.absolutePath
                                if (path.endsWith(".class") && !path.contains("/R\$") && !path.endsWith("/R.class") && !path.endsWith("/BuildConfig.class")) {
                                    if (NuwaSetUtils.isIncluded(path, includePackage)) {
                                        if (!NuwaSetUtils.isExcluded(path, excludeClass)) {
                                            //往class中注入字节码
                                            def bytes = NuwaProcessor.processClass(inputFile)
                                            path = path.split("${dirName}/")[1]
                                            def hash = DigestUtils.shaHex(bytes)
                                            hashFile.append(NuwaMapUtils.format(path, hash))

                                            //与上一个版本的hash值不一样就作为patch的组成部分
                                            if (NuwaMapUtils.notSame(hashMap, path, hash)) {
                                                NuwaFileUtils.copyBytesToFile(inputFile.bytes, NuwaFileUtils.touchFile(patchDir, path))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        def nuwaClassBeforeDexTask = project.tasks[nuwaClassBeforeDex]
                        nuwaClassBeforeDexTask.dependsOn dexTask.taskDependencies.getDependencies(dexTask)
                        dexTask.dependsOn nuwaClassBeforeDexTask

                        nuwaClassBeforeDexTask.doLast(copyMappingClosure)

                        nuwaPatchTask.dependsOn nuwaClassBeforeDexTask
                        beforeDexTasks.add(nuwaClassBeforeDexTask)

                    } else {
                        //开启Multidex
                        def nuwaJarBeforeDex = "nuwaJarBeforeDex${variant.name.capitalize()}"
                        project.task(nuwaJarBeforeDex) << {
                            Set<File> inputFiles = dexTask.inputs.files.files
                            inputFiles.each { inputFile ->
                                def path = inputFile.absolutePath
                                if (path.endsWith(".jar")) {
                                    NuwaProcessor.processJar(hashFile, inputFile, patchDir, hashMap, includePackage, excludeClass)
                                }
                            }
                        }
                        def nuwaJarBeforeDexTask = project.tasks[nuwaJarBeforeDex]
                        nuwaJarBeforeDexTask.dependsOn dexTask.taskDependencies.getDependencies(dexTask)
                        dexTask.dependsOn nuwaJarBeforeDexTask

                        nuwaJarBeforeDexTask.doFirst(nuwaPrepareClosure)
                        nuwaJarBeforeDexTask.doLast(copyMappingClosure)

                        nuwaPatchTask.dependsOn nuwaJarBeforeDexTask
                        beforeDexTasks.add(nuwaJarBeforeDexTask)

                    }

                }
            }

            project.task(NUWA_PATCHES) << {
                patchList.each { patchDir ->
                    NuwaAndroidUtils.dex(project, patchDir)
                }
            }
            beforeDexTasks.each {
                project.tasks[NUWA_PATCHES].dependsOn it
            }
        }
    }
}


