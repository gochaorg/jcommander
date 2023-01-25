package dist

sealed trait JavaExe {
  def exeName:String
}
object JavaExe {
  object console extends JavaExe {
    override def exeName: String = "\"!JAVA_EXE!\""
  }
  object window extends JavaExe {
    override def exeName: String = "\"!JAVAW_EXE!\""
  }
}

case class BatchScript(
  mainClass: String,
  javaExe: JavaExe = JavaExe.console,
  relativeScriptDepth: Int = 1,
  sysProps: List[JvmSysProp] = List.empty,
  jarsDirectory: String = "\\lib\\jar",
  dll32Directory: String = "\\lib\\dll\\win32",
  dll64Directory: String = "\\lib\\dll\\win64",
  appNameSysProp: String = "app.name",
  basedirSysProp: String = "basedir"
) {
  lazy val fullScript =
    headerScript +
    resolveAppHomeScript +
    determOsBitnessScript +
    javaLibPathScript +
    jarsDirectoryScript +
    systemPropertiesScript +
    findJvmBoundedScript +
    findJvmByShellScript +
    executeScript

  lazy val headerScript = 
   s"""|@echo off
       |
       |setlocal enabledelayedexpansion
       |setlocal enableextensions
       |
       |set ERROR_CODE=0
       |set BATCH_START_VERBOSE=0
       |set CMD_LINE_ARGS=%*
       |set THIS_DIR=%~dp0
       |set APPNAME=%~n0
       |if "%BATCH_START_VERBOSE%"=="1" echo APPNAME=!APPNAME!
       |
       |set DEFINE_SYSPROP_APPNAME=1
       |set DEFINE_SYSPROP_BASEDIR=1
       |
       |set "MAINCLASS=$mainClass"
       |""".stripMargin

  def relativeToAppHome:String = 
    relativeScriptDepth match {
      case _ if relativeScriptDepth <= 0 => ""
      case 1 => "..\\"
      case 2 => "..\\..\\"
      case _ => "..\\" * relativeScriptDepth
    }


  lazy val resolveAppHomeScript =
    s"""|@rem define home dir of app
        |if not defined APP_HOME (
        |	pushd .
        |	cd /d "%THIS_DIR%$relativeToAppHome"
        |	set "APP_HOME=!CD!"
        |	popd
        |	if "%BATCH_START_VERBOSE%"=="1" echo APP_HOME=!APP_HOME!
        |)
        |""".stripMargin

  lazy val determOsBitnessScript =
   s"""|@rem determinate OS bitness
       |set OS_BITNESS=64
       |if %PROCESSOR_ARCHITECTURE% == x86 (
       |  if not defined PROCESSOR_ARCHITEW6432 (
       |    set OS_BITNESS=32
       |  )
       |)
       |if "%BATCH_START_VERBOSE%"=="1" echo OS_BITNESS=!OS_BITNESS!
       |""".stripMargin

  lazy val javaLibPathScript = 
    s"""|@rem define java.library.path
        |set "PREDEF_WIN32_JAVA_LIBPATH=!APP_HOME!$dll32Directory"
        |set "PREDEF_WIN64_JAVA_LIBPATH=!APP_HOME!$dll64Directory"
        |if not defined JAVA_LIBPATH (
        |  if %OS_BITNESS% == 64 (
        |    if exist %PREDEF_WIN64_JAVA_LIBPATH% (
        |      set "JAVA_LIBPATH=%PREDEF_WIN64_JAVA_LIBPATH%"
        |    )
        |  )
        |  if %OS_BITNESS% == 32 (
        |    if exist %PREDEF_WIN32_JAVA_LIBPATH% (
        |      set "JAVA_LIBPATH=%PREDEF_WIN32_JAVA_LIBPATH%"
        |    )
        |  )
        |)
        |if "%BATCH_START_VERBOSE%"=="1" echo JAVA_LIBPATH=!JAVA_LIBPATH!
        |if defined JAVA_LIBPATH (
        |  if defined JAVA_OPTS (
        |    set "JAVA_OPTS=!JAVA_OPTS! -Djava.library.path=!JAVA_LIBPATH!"
        |  ) else (
        |    set "JAVA_OPTS=-Djava.library.path=!JAVA_LIBPATH!"
        |  )
        |)
        |""".stripMargin

  lazy val jarsDirectoryScript =
    s"""|@rem add *.jar to classpath
        |set "JARS_DIR=!APP_HOME!$jarsDirectory"
        |if "%BATCH_START_VERBOSE%"=="1" echo JARS_DIR=!JARS_DIR!
        |if defined CLASSPATH (
        |  set "CLASSPATH=!JARS_DIR!\\*;!CLASSPATH!"
        |  if "%BATCH_START_VERBOSE%"=="1" echo append JARS_DIR to CLASSPATH
        |)
        |if not defined CLASSPATH (
        |  set "CLASSPATH=!JARS_DIR!\\*"
        |  if "%BATCH_START_VERBOSE%"=="1" echo set JARS_DIR to CLASSPATH
        |)
        |""".stripMargin

  lazy val systemPropertiesScript =
    s"""|@rem set -Dapp.name -Dbasedir
        |if "%DEFINE_SYSPROP_APPNAME%"=="1" (
        |  if defined JAVA_OPTS (
        |    set "JAVA_OPTS=!JAVA_OPTS! -D$appNameSysProp=!APPNAME!"
        |    if "%BATCH_START_VERBOSE%"=="1" echo append -D$appNameSysProp=!APPNAME! to JAVA_OPTS
        |  ) else (
        |    set "JAVA_OPTS=-D$appNameSysProp=!APPNAME!"
        |    if "%BATCH_START_VERBOSE%"=="1" echo set -D$appNameSysProp=!APPNAME! to JAVA_OPTS
        |  )
        |)
        |if "%DEFINE_SYSPROP_BASEDIR%"=="1" (
        |  if defined JAVA_OPTS (
        |    set "JAVA_OPTS=!JAVA_OPTS! -D$basedirSysProp=!APP_HOME!"
        |    if "%BATCH_START_VERBOSE%"=="1" echo append -D$basedirSysProp=!APP_HOME! to JAVA_OPTS
        |  ) else (
        |    set "JAVA_OPTS=-D$basedirSysProp=!APP_HOME!"
        |    if "%BATCH_START_VERBOSE%"=="1" echo set -D$basedirSysProp=!APP_HOME! to JAVA_OPTS
        |  )
        |)
        |""".stripMargin + {
          sysProps.map { value => 
            s"""|if defined JAVA_OPTS (
                |  set "JAVA_OPTS=!JAVA_OPTS! ${value.cmdLine}"
                |  if "%BATCH_START_VERBOSE%"=="1" echo append ${value.cmdLine} to JAVA_OPTS
                |) else (
                |  set "JAVA_OPTS=${value.cmdLine}"
                |  if "%BATCH_START_VERBOSE%"=="1" echo set ${value.cmdLine} to JAVA_OPTS
                |)
                |""".stripMargin
          }.mkString("\n")
        }

  lazy val findJvmBoundedScript = 
    """|@rem determinate JAVA_HOME, JAVA_EXE, JAVAW_EXE
       |if not defined JAVA_HOME (
       |  pushd .
       |  if exist "!APP_HOME!\java\bin\java.exe" (
       |    cd /d "!APP_HOME!\java"
       |    set "JAVA_HOME=!CD!"
       |  )
       |  if exist "!APP_HOME!\..\java\bin\java.exe" (
       |    cd /d "!APP_HOME!\..\java"
       |    set "JAVA_HOME=!CD!"
       |  )
       |  if %OS_BITNESS% == 32 (
       |    if exist "!APP_HOME!\java\win32\bin\java.exe" (
       |      cd /d "!APP_HOME!\java\win32"
       |      set "JAVA_HOME=!CD!"
       |    )
       |    if exist "!APP_HOME!\..\java\win32\bin\java.exe" (
       |      cd /d "!APP_HOME!\..\java\win32"
       |      set "JAVA_HOME=!CD!"
       |    )
       |  )
       |  if %OS_BITNESS% == 64 (
       |    if exist "!APP_HOME!\java\win64\bin\java.exe" (
       |      cd /d "!APP_HOME!\java\win64"
       |      set "JAVA_HOME=!CD!"
       |    )
       |    if exist "!APP_HOME!\..\java\win64\bin\java.exe" (
       |      cd /d "!APP_HOME!\..\java\win64"
       |      set "JAVA_HOME=!CD!"
       |    )
       |  )
       |  popd
       |)
       |""".stripMargin

  lazy val findJvmByShellScript =
    """|if not defined JAVA_HOME (
       |  java.exe -version >NUL 2>&1
       |  if errorlevel 1 (
       |    echo install JAVA or set JAVA_HOME
       |    exit /b 1
       |  )
       |  set JAVA_EXE=java.exe
       |  set JAVAW_EXE=javaw.exe
       |) else (
       |  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
       |  set "JAVAW_EXE=%JAVA_HOME%\bin\javaw.exe"
       |  if "%BATCH_START_VERBOSE%"=="1" echo JAVA_HOME=!JAVA_HOME!
       |)
       |""".stripMargin

  lazy val executeScript=
    s"""|@rem execute
        |${javaExe.exeName} -cp "!CLASSPATH!" !JAVA_OPTS! !MAINCLASS! %CMD_LINE_ARGS%
        |if %ERRORLEVEL% NEQ 0 goto error
        |goto end
        |
        |:error
        |if "%OS%"=="Windows_NT" @endlocal
        |set ERROR_CODE=%ERRORLEVEL%
        |
        |:end
        |:postExec
        |if "%FORCE_EXIT_ON_ERROR%" == "on" (
        |  if %ERROR_CODE% NEQ 0 exit %ERROR_CODE%
        |)
        |
        |exit /B %ERROR_CODE%
        |""".stripMargin
}
