#! /bin/sh
#A script for generating ChangeLog with the output from git. The script will only run if the current ChangeLog contains exactly one line.
# Originally from http://git.worldforge.org/?p=ember.git;a=blob;f=generate-ChangeLog.sh which is GPLv3
#This makes it possible to do a new "ant dist" from an existing source distribution (as the ChangeLog would then be complete.).
top_srcdir=$1
distdir=$2
if [ x${distdir} = "x" ] || [ x${top_srcdir} = "x" ]; then
	echo "This script will generate a ChangeLog from the output from git log. It therefore needs to be run in a git source directory."
	echo "Params: <source directory path> <distribution directory path>"	
	exit 1
fi
#Only do the aggregation if the ChangeLog file is exactly one line. If not the aggregation has already been done.
if [ `cat ${distdir}/ChangeLog | wc -l` = "1" ]; then
	echo "Generating ChangeLog by generating it from git. This requires that you create the dist in the git repository."

	chmod u+w ${distdir}/ChangeLog && git log  --stat --name-only --date=short --abbrev-commit > ${distdir}/ChangeLog
fi
