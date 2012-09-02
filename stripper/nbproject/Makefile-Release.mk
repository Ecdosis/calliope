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
CND_CONF=Release
CND_DISTDIR=dist
CND_BUILDDIR=build

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=${CND_BUILDDIR}/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/src/xmltok_impl.o \
	${OBJECTDIR}/src/recipe.o \
	${OBJECTDIR}/src/range.o \
	${OBJECTDIR}/src/stack.o \
	${OBJECTDIR}/src/STIL.o \
	${OBJECTDIR}/src/stripper.o \
	${OBJECTDIR}/src/HRIT.o \
	${OBJECTDIR}/src/simplification.o \
	${OBJECTDIR}/src/error.o \
	${OBJECTDIR}/src/xmlparse.o \
	${OBJECTDIR}/src/xmltok.o \
	${OBJECTDIR}/src/ramfile.o \
	${OBJECTDIR}/src/attribute.o \
	${OBJECTDIR}/src/xmlrole.o \
	${OBJECTDIR}/src/hashset.o \
	${OBJECTDIR}/src/xmltok_ns.o


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
LDLIBSOPTIONS=

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/stripper

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/stripper: ${OBJECTFILES}
	${MKDIR} -p ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}
	${LINK.c} -o ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/stripper ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/src/xmltok_impl.o: src/xmltok_impl.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/xmltok_impl.o src/xmltok_impl.c

${OBJECTDIR}/src/recipe.o: src/recipe.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/recipe.o src/recipe.c

${OBJECTDIR}/src/range.o: src/range.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/range.o src/range.c

${OBJECTDIR}/src/stack.o: src/stack.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/stack.o src/stack.c

${OBJECTDIR}/src/STIL.o: src/STIL.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/STIL.o src/STIL.c

${OBJECTDIR}/src/stripper.o: src/stripper.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/stripper.o src/stripper.c

${OBJECTDIR}/src/HRIT.o: src/HRIT.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/HRIT.o src/HRIT.c

${OBJECTDIR}/src/simplification.o: src/simplification.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/simplification.o src/simplification.c

${OBJECTDIR}/src/error.o: src/error.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/error.o src/error.c

${OBJECTDIR}/src/xmlparse.o: src/xmlparse.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/xmlparse.o src/xmlparse.c

${OBJECTDIR}/src/xmltok.o: src/xmltok.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/xmltok.o src/xmltok.c

${OBJECTDIR}/src/ramfile.o: src/ramfile.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/ramfile.o src/ramfile.c

${OBJECTDIR}/src/attribute.o: src/attribute.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/attribute.o src/attribute.c

${OBJECTDIR}/src/xmlrole.o: src/xmlrole.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/xmlrole.o src/xmlrole.c

${OBJECTDIR}/src/hashset.o: src/hashset.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/hashset.o src/hashset.c

${OBJECTDIR}/src/xmltok_ns.o: src/xmltok_ns.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/xmltok_ns.o src/xmltok_ns.c

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
