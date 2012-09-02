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
CND_PLATFORM=GNU-MacOSX
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
	${OBJECTDIR}/src/node.o \
	${OBJECTDIR}/src/range.o \
	${OBJECTDIR}/src/css_parse.o \
	${OBJECTDIR}/src/main.o \
	${OBJECTDIR}/src/css_selector.o \
	${OBJECTDIR}/src/HTML.o \
	${OBJECTDIR}/src/STIL/cJSON.o \
	${OBJECTDIR}/src/matrix.o \
	${OBJECTDIR}/src/master.o \
	${OBJECTDIR}/src/text_buf.o \
	${OBJECTDIR}/src/css_property.o \
	${OBJECTDIR}/src/css_rule.o \
	${OBJECTDIR}/src/matrix_queue.o \
	${OBJECTDIR}/src/formatter.o \
	${OBJECTDIR}/src/hashmap.o \
	${OBJECTDIR}/src/error.o \
	${OBJECTDIR}/src/xmlparse.o \
	${OBJECTDIR}/src/xmltok.o \
	${OBJECTDIR}/src/file_list.o \
	${OBJECTDIR}/src/memwatch.o \
	${OBJECTDIR}/src/attribute.o \
	${OBJECTDIR}/src/xmlrole.o \
	${OBJECTDIR}/src/HRIT/HRIT.o \
	${OBJECTDIR}/src/hashset.o \
	${OBJECTDIR}/src/dom.o \
	${OBJECTDIR}/src/STIL/memory.o \
	${OBJECTDIR}/src/range_array.o \
	${OBJECTDIR}/src/annotation.o \
	${OBJECTDIR}/src/STIL/STIL.o \
	${OBJECTDIR}/src/jni.o \
	${OBJECTDIR}/src/xmltok_ns.o \
	${OBJECTDIR}/src/queue.o


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
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/formatter

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/formatter: ${OBJECTFILES}
	${MKDIR} -p ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}
	${LINK.c} -o ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/formatter ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/src/xmltok_impl.o: src/xmltok_impl.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/xmltok_impl.o src/xmltok_impl.c

${OBJECTDIR}/src/node.o: src/node.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/node.o src/node.c

${OBJECTDIR}/src/range.o: src/range.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/range.o src/range.c

${OBJECTDIR}/src/css_parse.o: src/css_parse.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/css_parse.o src/css_parse.c

${OBJECTDIR}/src/main.o: src/main.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/main.o src/main.c

${OBJECTDIR}/src/css_selector.o: src/css_selector.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/css_selector.o src/css_selector.c

${OBJECTDIR}/src/HTML.o: src/HTML.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/HTML.o src/HTML.c

${OBJECTDIR}/src/STIL/cJSON.o: src/STIL/cJSON.c 
	${MKDIR} -p ${OBJECTDIR}/src/STIL
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/STIL/cJSON.o src/STIL/cJSON.c

${OBJECTDIR}/src/matrix.o: src/matrix.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/matrix.o src/matrix.c

${OBJECTDIR}/src/master.o: src/master.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/master.o src/master.c

${OBJECTDIR}/src/text_buf.o: src/text_buf.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/text_buf.o src/text_buf.c

${OBJECTDIR}/src/css_property.o: src/css_property.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/css_property.o src/css_property.c

${OBJECTDIR}/src/css_rule.o: src/css_rule.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/css_rule.o src/css_rule.c

${OBJECTDIR}/src/matrix_queue.o: src/matrix_queue.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/matrix_queue.o src/matrix_queue.c

${OBJECTDIR}/src/formatter.o: src/formatter.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/formatter.o src/formatter.c

${OBJECTDIR}/src/hashmap.o: src/hashmap.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/hashmap.o src/hashmap.c

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

${OBJECTDIR}/src/file_list.o: src/file_list.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/file_list.o src/file_list.c

${OBJECTDIR}/src/memwatch.o: src/memwatch.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/memwatch.o src/memwatch.c

${OBJECTDIR}/src/attribute.o: src/attribute.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/attribute.o src/attribute.c

${OBJECTDIR}/src/xmlrole.o: src/xmlrole.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/xmlrole.o src/xmlrole.c

${OBJECTDIR}/src/HRIT/HRIT.o: src/HRIT/HRIT.c 
	${MKDIR} -p ${OBJECTDIR}/src/HRIT
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/HRIT/HRIT.o src/HRIT/HRIT.c

${OBJECTDIR}/src/hashset.o: src/hashset.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/hashset.o src/hashset.c

${OBJECTDIR}/src/dom.o: src/dom.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/dom.o src/dom.c

${OBJECTDIR}/src/STIL/memory.o: src/STIL/memory.c 
	${MKDIR} -p ${OBJECTDIR}/src/STIL
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/STIL/memory.o src/STIL/memory.c

${OBJECTDIR}/src/range_array.o: src/range_array.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/range_array.o src/range_array.c

${OBJECTDIR}/src/annotation.o: src/annotation.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/annotation.o src/annotation.c

${OBJECTDIR}/src/STIL/STIL.o: src/STIL/STIL.c 
	${MKDIR} -p ${OBJECTDIR}/src/STIL
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/STIL/STIL.o src/STIL/STIL.c

${OBJECTDIR}/src/jni.o: src/jni.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/jni.o src/jni.c

${OBJECTDIR}/src/xmltok_ns.o: src/xmltok_ns.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/xmltok_ns.o src/xmltok_ns.c

${OBJECTDIR}/src/queue.o: src/queue.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/queue.o src/queue.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/formatter

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
