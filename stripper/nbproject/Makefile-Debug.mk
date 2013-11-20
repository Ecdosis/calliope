#
# Generated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add customized code.
#
# This makefile implements configuration specific macros and targets.


# Environment
MKDIR=mkdir
CP=cp
GREP=grep
NM=nm
CCADMIN=CCadmin
RANLIB=ranlib
CC=gcc
CCC=g++
CXX=g++
FC=gfortran
AS=as

# Macros
CND_PLATFORM=GNU-Linux-x86
CND_DLIB_EXT=so
CND_CONF=Debug
CND_DISTDIR=dist
CND_BUILDDIR=build

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=${CND_BUILDDIR}/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/_ext/1858218211/cJSON.o \
	${OBJECTDIR}/_ext/1078767344/memwatch.o \
	${OBJECTDIR}/src/AESE.o \
	${OBJECTDIR}/src/STIL.o \
	${OBJECTDIR}/src/attribute.o \
	${OBJECTDIR}/src/dest_file.o \
	${OBJECTDIR}/src/error.o \
	${OBJECTDIR}/src/hashmap.o \
	${OBJECTDIR}/src/hashset.o \
	${OBJECTDIR}/src/hh_exceptions.o \
	${OBJECTDIR}/src/layer.o \
	${OBJECTDIR}/src/log.o \
	${OBJECTDIR}/src/milestone.o \
	${OBJECTDIR}/src/ramfile.o \
	${OBJECTDIR}/src/range.o \
	${OBJECTDIR}/src/recipe.o \
	${OBJECTDIR}/src/simplification.o \
	${OBJECTDIR}/src/stack.o \
	${OBJECTDIR}/src/stripper.o \
	${OBJECTDIR}/src/userdata.o \
	${OBJECTDIR}/src/utils.o


# C Compiler Flags
CFLAGS=

# CC Compiler Flags
CCFLAGS=
CXXFLAGS=

# Fortran Compiler Flags
FFLAGS=

# Assembler Flags
ASFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=-laspell -lexpat -lm -ltidy

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/stripper

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/stripper: ${OBJECTFILES}
	${MKDIR} -p ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}
	${LINK.c} -o ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/stripper ${OBJECTFILES} ${LDLIBSOPTIONS}

${OBJECTDIR}/_ext/1858218211/cJSON.o: ../formatter/src/STIL/cJSON.c 
	${MKDIR} -p ${OBJECTDIR}/_ext/1858218211
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/_ext/1858218211/cJSON.o ../formatter/src/STIL/cJSON.c

${OBJECTDIR}/_ext/1078767344/memwatch.o: ../formatter/src/memwatch.c 
	${MKDIR} -p ${OBJECTDIR}/_ext/1078767344
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/_ext/1078767344/memwatch.o ../formatter/src/memwatch.c

${OBJECTDIR}/src/AESE.o: src/AESE.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/AESE.o src/AESE.c

${OBJECTDIR}/src/STIL.o: src/STIL.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/STIL.o src/STIL.c

${OBJECTDIR}/src/attribute.o: src/attribute.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/attribute.o src/attribute.c

${OBJECTDIR}/src/dest_file.o: src/dest_file.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/dest_file.o src/dest_file.c

${OBJECTDIR}/src/error.o: src/error.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/error.o src/error.c

${OBJECTDIR}/src/hashmap.o: src/hashmap.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/hashmap.o src/hashmap.c

${OBJECTDIR}/src/hashset.o: src/hashset.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/hashset.o src/hashset.c

${OBJECTDIR}/src/hh_exceptions.o: src/hh_exceptions.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/hh_exceptions.o src/hh_exceptions.c

${OBJECTDIR}/src/layer.o: src/layer.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/layer.o src/layer.c

${OBJECTDIR}/src/log.o: src/log.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/log.o src/log.c

${OBJECTDIR}/src/milestone.o: src/milestone.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/milestone.o src/milestone.c

${OBJECTDIR}/src/ramfile.o: src/ramfile.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/ramfile.o src/ramfile.c

${OBJECTDIR}/src/range.o: src/range.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/range.o src/range.c

${OBJECTDIR}/src/recipe.o: src/recipe.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/recipe.o src/recipe.c

${OBJECTDIR}/src/simplification.o: src/simplification.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/simplification.o src/simplification.c

${OBJECTDIR}/src/stack.o: src/stack.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/stack.o src/stack.c

${OBJECTDIR}/src/stripper.o: src/stripper.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/stripper.o src/stripper.c

${OBJECTDIR}/src/userdata.o: src/userdata.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/userdata.o src/userdata.c

${OBJECTDIR}/src/utils.o: src/utils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -g -DCOMMANDLINE -DMEMWATCH -Iinclude -I../formatter/include/STIL -I../formatter/include -I/usr/lib/jvm/java-7-openjdk-amd64/include -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/utils.o src/utils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/stripper

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
