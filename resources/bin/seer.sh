#!/bin/sh

# Main Script for the Network Seer
#
# Environment Variable Prequisites
#
#   SEER_HOME   Home of Network Seer installation. If not set I will  try
#                   to figure it out.
#
#   JAVA_HOME       Must point at your Java Development Kit installation.
#
#   JAVA_OPTS       (Optional) Java runtime options used when the commands
#                   is executed.
#
# NOTE: Borrowed generously from Apache Tomcat startup scripts.
# -----------------------------------------------------------------------------

# OS specific support.  $var _must_ be set to either true or false.
#ulimit -n 100000

cygwin=false;
darwin=false;
os400=false;
mingw=false;
case "`uname`" in
CYGWIN*) cygwin=true;;
MINGW*) mingw=true;;
OS400*) os400=true;;
Darwin*) darwin=true
        if [ -z "$JAVA_VERSION" ] ; then
             JAVA_VERSION="CurrentJDK"
           else
             echo "Using Java version: $JAVA_VERSION"
           fi
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/${JAVA_VERSION}/Home
           fi
           ;;
esac

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set SEER_HOME if not already set
[ -z "$SEER_HOME" ] && SEER_HOME=`cd "$PRGDIR/.." ; pwd`


# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$SEER_HOME" ] && SEER_HOME=`cygpath --unix "$SEER_HOME"`
fi

# For OS400
if $os400; then
  # Set job priority to standard for interactive (interactive - 6) by using
  # the interactive priority - 6, the helper threads that respond to requests
  # will be running at the same priority as interactive jobs.
  COMMAND='chgjob job('$JOBNAME') runpty(6)'
  system $COMMAND

  # Enable multi threading
  QIBM_MULTI_THREADED=Y
  export QIBM_MULTI_THREADED
fi

# For Migwn, ensure paths are in UNIX format before anything is touched
if $mingw ; then
  [ -n "$SEER_HOME" ] &&
    SEER_HOME="`(cd "$SEER_HOME"; pwd)`"
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME="`(cd "$JAVA_HOME"; pwd)`"
fi

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=java
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo " SEER cannot execute $JAVACMD"
  exit 1
fi

# if JAVA_HOME is not set we're not happy
if [ -z "$JAVA_HOME" ]; then
  echo "You must set the JAVA_HOME variable before running SEER."
  exit 1
fi

if [ -e "$SEER_HOME/seer.pid" ]; then
  PID=`cat "$SEER_HOME"/seer.pid`
fi

# ----- Process the input command ----------------------------------------------
args=""
for c in $*
do
    if [ "$c" = "--debug" ] || [ "$c" = "-debug" ] || [ "$c" = "debug" ]; then
          CMD="--debug"
          continue
    elif [ "$CMD" = "--debug" ]; then
          if [ -z "$PORT" ]; then
                PORT=$c
          fi
    elif [ "$c" = "--stop" ] || [ "$c" = "-stop" ] || [ "$c" = "stop" ]; then
          CMD="stop"
    elif [ "$c" = "--start" ] || [ "$c" = "-start" ] || [ "$c" = "start" ]; then
          CMD="start"
    elif [ "$c" = "--version" ] || [ "$c" = "-version" ] || [ "$c" = "version" ]; then
          CMD="version"
    elif [ "$c" = "--restart" ] || [ "$c" = "-restart" ] || [ "$c" = "restart" ]; then
          CMD="restart"
    elif [ "$c" = "--test" ] || [ "$c" = "-test" ] || [ "$c" = "test" ]; then
          CMD="test"
    else
        args="$args $c"
    fi
done

if [ "$CMD" = "--debug" ]; then
  if [ "$PORT" = "" ]; then
    echo " Please specify the debug port after the --debug option"
    exit 1
  fi
  if [ -n "$JAVA_OPTS" ]; then
    echo "Warning !!!. User specified JAVA_OPTS will be ignored, once you give the --debug option."
  fi
  CMD="RUN"
  JAVA_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=$PORT"
  echo "Please start the remote debugging client to continue..."
elif [ "$CMD" = "start" ]; then
  if [ -e "$SEER_HOME/seer.pid" ]; then
    if  ps -p $PID > /dev/null ; then
      echo "Process is already running"
      exit 0
    fi
  fi
  export SEER_HOME="$SEER_HOME"
# using nohup sh to avoid erros in solaris OS.TODO
  nohup sh "$SEER_HOME"/bin/seer.sh $args > /dev/null 2>&1 &
  exit 0
elif [ "$CMD" = "stop" ]; then
  export SEER_HOME="$SEER_HOME"
  kill -term `cat "$SEER_HOME"/seer.pid`
  exit 0
elif [ "$CMD" = "restart" ]; then
  export SEER_HOME="$SEER_HOME"
  kill -term `cat "$SEER_HOME"/seer.pid`
  process_status=0
  pid=`cat "$SEER_HOME"/seer.pid`
  while [ "$process_status" -eq "0" ]
  do
        sleep 1;
        ps -p$pid 2>&1 > /dev/null
        process_status=$?
  done

# using nohup sh to avoid erros in solaris OS.TODO
  nohup sh "$SEER_HOME"/bin/seer.sh $args > /dev/null 2>&1 &
  exit 0
elif [ "$CMD" = "test" ]; then
    JAVACMD="exec "$JAVACMD""
elif [ "$CMD" = "version" ]; then
  cat "$SEER_HOME"/bin/version.txt
  exit 0
fi

# ---------- Handle the SSL Issue with proper JDK version --------------------
jdk_17=`$JAVA_HOME/bin/java -version 2>&1 | grep "1.[8|9]"`
if [ "$jdk_17" = "" ]; then
   echo " Starting Network Seer (in untested JDK)"
   echo " [ERROR] SEER is supported only on JDK 1.8 and 1.9"
fi

JAVA_ENDORSED_DIRS="$SEER_HOME/../lib/endorsed":"$JAVA_HOME/jre/lib/endorsed":"$JAVA_HOME/lib/endorsed"

SEER_CLASSPATH=""
if [ -e "$JAVA_HOME/lib/tools.jar" ]; then
    SEER_CLASSPATH="$JAVA_HOME/../lib/tools.jar"
fi
for f in "$SEER_HOME"/runtime/lib/*.jar
do
    if [ "$f" != "$SEER_HOME/runtime/lib/*.jar" ];then
        SEER_CLASSPATH="$SEER_CLASSPATH":$f
    fi
done

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --absolute --windows "$JAVA_HOME"`
  SEER_HOME=`cygpath --absolute --windows "$SEER_HOME"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
  JAVA_ENDORSED_DIRS=`cygpath --path --windows "$JAVA_ENDORSED_DIRS"`
  SEER_CLASSPATH=`cygpath --path --windows "$SEER_CLASSPATH"`
fi

# ----- Execute The Requested Command -----------------------------------------

echo JAVA_HOME environment variable is set to $JAVA_HOME
echo SEER_HOME environment variable is set to "$SEER_HOME"

cd "$SEER_HOME"

TMP_DIR="$SEER_HOME"/tmp
if [ -d "$TMP_DIR" ]; then
rm -rf "$TMP_DIR"/*
fi

START_EXIT_STATUS=121
status=$START_EXIT_STATUS

if [ -z "$JVM_MEM_OPTS" ]; then
   java_version=$("$JAVACMD" -version 2>&1 | awk -F '"' '/version/ {print $2}')
   JVM_MEM_OPTS="-Xms256m -Xmx1024m"
   if [ "$java_version" \< "1.8" ]; then
      JVM_MEM_OPTS="$JVM_MEM_OPTS -XX:MaxPermSize=256m"
   fi
fi
echo "Using Java memory options: $JVM_MEM_OPTS"

while [ "$status" = "$START_EXIT_STATUS" ]
do
    $JAVACMD \
    -Xbootclasspath/a:"$SEER_XBOOTCLASSPATH" \
    $JVM_MEM_OPTS \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath="$SEER_HOME/logs/heap-dump.hprof" \
    $JAVA_OPTS \
    -classpath "$SEER_CLASSPATH" \
    -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" \
    -Djava.io.tmpdir="$SEER_HOME/tmp" \
    -Djava.command="$JAVACMD" \
    -Dseer.home="$SEER_HOME" \
    -Dseer.config.dir.path="$SEER_HOME/conf" \
    -Dseer.datasources.dir.path="$SEER_HOME/datasources" \
    -Dseer.logs.dir.path="$SEER_HOME/logs" \
    -Dfile.encoding=UTF8 \
    -Djava.net.preferIPv4Stack=true \
    com.networkseer.core.SeerCore
    status=$?
done
