#!/bin/bash
pid=`cat imputation-server.pid`

if [ ! -e /proc/$pid -a /proc/$pid/exe ]; then
	echo "imputation server is NOT running."
else
	echo "imputation server is running."
fi
