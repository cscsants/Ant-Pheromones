#!/bin/sh
# simple wrapper script to do gui runs ...e.g.,
#     bin/guirun.sh D=3  

# This first is the path to the location of all the parts of the project,
# eg, where the src/  classes/ bin/ dirs are located.
# CHANGE THIS after copy/move the project ==>
PROJECTDIR=/users/nglange/project/









# the parameters below only occassionally need to be changed
PACKAGENAME=AntPheromones
GUIMODELNAME=GUIModel


# add extra user libraries that should be included here
USERDEFINEDLIBS=

# add a run time parameter to java here
JAVAPAR=

########################################################################
########################################################################
# PROBABLY NO CHANGES BELOW THIS

# the params below should be changed when versions of java
# and different libraries are upgraded so they point to the
# correct paths of java and libraries

# java path -- linux or mac
case $(uname) in
   Darwin)
      JAVADIR="/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home"
      ;;
   Linux)
      JAVADIR=/appl64/jdk1.6.0_11
      ;;
   *)
      JAVADIR=/appl/jdk1.6.0_10
      ;;
esac 

CSCS530LIBDIR=/users/rlr/Courses/

# the actual run command 
$JAVADIR/bin/java $JAVAPAR -cp $USERDEFINEDLIBS:$PROJECTDIR/bin:$CSCS530LIBDIR/cscs530.jar  $PACKAGENAME.$GUIMODELNAME $*

