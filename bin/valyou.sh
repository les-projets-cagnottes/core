#!/bin/bash

function d_start ()
{
	echo "Valyou: starting service"
	BASEDIR=$(dirname "$0")/..
	cd "$BASEDIR" &&
	. bin/setenv.sh &&
	nohup java -Dconfig.location=application.properties -jar valyou-api.jar > logs/valyou-api.log 2>&1 & echo $! > /var/run/valyou-api.pid & sleep 5
}

function d_stop ()
{
	echo "Valyou: stopping service"
	cat /var/run/valyou-api.pid | xargs kill -9
 }

function d_status ( )
{
	ps -ef | grep node | grep -v grep
}

case "$1" in
	start)
		d_start
		;;
	stop)
		d_stop
		;;
	reload)
		d_stop
		sleep 2
		d_start
		;;
	status)
		d_status
		;;
	* )
	echo "Usage: $0 {start | stop | reload | status}"
	exit 1
	;;
esac

exit 0