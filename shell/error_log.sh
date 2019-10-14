#！/bin/bash

TODAY_STR=`date +"%Y-%m-%d"`
BASE_DIRECTORY="xxxxxx"
ERROR_LOG_NAME="xxxxxx"
PROJECTS=()

function checkProject {
	ERROR_LOG_PATH=$1"/"$ERROR_LOG_NAME
	if [ -f $ERROR_LOG_PATH ];
	then
		LOG_CHANGE_DATE=`stat $ERROR_LOG_PATH | sed -n '/Change/p' | awk '{print $2}'`
		if [ $LOG_CHANGE_DATE == $TODAY_STR ];
		then 
			PROJECTS[${#PROJECTS[*]}]=$2
		fi
	else 
		echo \'$ERROR_LOG_PATH\' not exist
	fi
}

if [ -d $BASE_DIRECTORY ];
then 
	for PROJECT in `ls $BASE_DIRECTORY`
	do
		PROJECT_DIRECTORY=$BASE_DIRECTORY"/"$PROJECT
		if [ -d $PROJECT_DIRECTORY ];
		then 
			checkProject $PROJECT_DIRECTORY $PROJECT
		fi
	done
	TABLE="<table style='border: solid 1px #000000;'>"
	for PROJECT_ITEM in ${PROJECTS[*]}
	do
		TABLE=$TABLE"<tr><td>"$PROJECT_ITEM"</td></tr>"
	done
	TABLE=$TABLE"</table>"
	BODY="<div>如下工程出现新的错误日志:</div>"$TABLE
	TITLE="[xxxxxx]日常维护提醒_"$TODAY_STR
	echo $BODY | mutt -s $TITLE -e 'set content_type="text/html"' xxxxxx@xxx.xxx
else 
	echo \'$BASE_DIRECTORY\' not exist
fi
