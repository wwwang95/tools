#!/bin/bash

function showMenu {
	echo "FREE YOUR HANDS:)"
	echo "-----------------------------"
	echo "1. Install WireGuard"
	echo "2. Restart WireGuard"
	echo "3. Modify WireGuard UDP port"
	echo "4. Display client configuration QR code"
	echo "5. Display connections"
	echo "0. Quit"
	echo "-----------------------------"
	echo -n "Enter the item number: "
}

function checkLibAvailable {
	if [ "`rpm -q $1 | awk '{if(/^package.*is not installed$/){print 1}}'`" ]
	then
		yum install -y $1
	fi
}

function install {
	checkLibAvailable curl
	if [ ! -f "/etc/yum.repos.d/wireguard.repo" ]
	then
		curl -Lo /etc/yum.repos.d/wireguard.repo https://copr.fedorainfracloud.org/coprs/jdoss/wireguard/repo/epel-7/jdoss-wireguard-epel-7.repo
	fi
	checkLibAvailable epel-release
	checkLibAvailable wireguard-dkms
	checkLibAvailable wireguard-tools
	
	if [ -z "`cat /etc/sysctl.conf | sed -n '/net.ipv4.ip_forward/p' | uniq`" ]
	then
		echo "net.ipv4.ip_forward = 1" >> /etc/sysctl.conf
		sysctl -p
	fi
	
	if [ ! -d "/etc/wireguard" ]
	then
		mkdir /etc/wireguard
	fi
	umask 077
	
	if [ ! -f "/etc/wireguard/pri_server" ]
	then
		wg genkey >> /etc/wireguard/pri_server
		chmod 644 /etc/wireguard/pri_server
	fi
	
	if [ ! -f "/etc/wireguard/pri_client" ]
	then
		wg genkey >> /etc/wireguard/pri_client
		chmod 644 /etc/wireguard/pri_client
	fi
	
	echo -n "Enter an unused UDP port for WireGuard configuration[10000-65535]:"
	read port
	if [ $port -ge 10000 -a $port -le 65535 ] 2>/dev/null
	then
	    checkLibAvailable net-tools
		if [ -z "`netstat -unlp | grep $port`" ]
		then
			NOW_STR=`date +"%Y%m%d%H%M%S"`
			if [ -f "/etc/wireguard/wg_server.conf" ]
			then
				mv /etc/wireguard/wg_server.conf /etc/wireguard/wg_server-$NOW_STR.conf
			fi
			touch /etc/wireguard/wg_server.conf
			chmod 700 /etc/wireguard/wg_server.conf
			echo -e "[Interface]\nAddress = 10.0.0.1/24\nListenPort = $port\nMTU = 1500\nPrivateKey = $(cat /etc/wireguard/pri_server)" >> /etc/wireguard/wg_server.conf
			ethName=`ls -l /sys/class/net/ | grep pci0000:00 | awk -F " " '{print $9}' | head -n 1`
			echo -e "PostUp = iptables -A FORWARD -i %i -j ACCEPT; iptables -A FORWARD -o %i -j ACCEPT; iptables -t nat -A POSTROUTING -o $ethName -j MASQUERADE" >> /etc/wireguard/wg_server.conf
			echo -e "PostDown = iptables -D FORWARD -i %i -j ACCEPT; iptables -D FORWARD -o %i -j ACCEPT; iptables -t nat -D POSTROUTING -o $ethName -j MASQUERADE\n" >> /etc/wireguard/wg_server.conf
			echo -e "[Peer]\nPublicKey = $(wg pubkey < /etc/wireguard/pri_client)\nAllowedIPs = 10.0.0.2/32\n" >> /etc/wireguard/wg_server.conf
			
			if [ -f "/etc/wireguard/wg_client.conf" ]
			then
				mv /etc/wireguard/wg_client.conf /etc/wireguard/wg_client-$NOW_STR.conf
			fi
			touch /etc/wireguard/wg_client.conf
			chmod 700 /etc/wireguard/wg_client.conf
			echo -e "[Interface]\nAddress = 10.0.0.2/24\nPrivateKey = $(cat /etc/wireguard/pri_client)\nDNS = 8.8.8.8\n" >> /etc/wireguard/wg_client.conf
			ipAddress=`ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p' | head -n 1`
			echo -e "[Peer]\nPublicKey = $(wg pubkey < /etc/wireguard/pri_server)\nAllowedIPs = 0.0.0.0/0\nEndpoint = $ipAddress:$port\nPersistentKeepalive = 20" >> /etc/wireguard/wg_client.conf
			iptables -F
			wg-quick up wg_server
			echo "-----------------------------"
			echo "the client conf file is /etc/wireguard/wg_client.conf"
			echo "the qr code of client conf is:"
			checkLibAvailable qrencode
			qrencode -t ANSIUTF8 < /etc/wireguard/wg_client.conf
			echo "-----------------------------"
		else
			echo "port $port has been used."
		fi
	else
		echo "invalid port."
	fi
}

function restart {
	if [ "`ifconfig | grep wg_server`" ]
	then
		wg-quick down wg_server
	fi
	wg-quick up wg_server
}

function modifyPort {
	echo "this function will be done soon or just help yourself :)"
}

function showQRCode {
	if [ -f "/etc/wireguard/wg_client.conf" ]
	then
		checkLibAvailable qrencode
		qrencode -t ANSIUTF8 < /etc/wireguard/wg_client.conf
	else
		echo "there is no client conf file"
	fi
}

function readMenuNumber {
	read num
	if [ $num == 1 ];
	then 
		install
	elif [ $num == 2 ]
	then
		restart
	elif [ $num == 3 ]
	then
		modifyPort
	elif [ $num == 4 ]
	then
		showQRCode
	elif [ $num == 5 ]
    then
    	wg
	elif [ $num == 0 ]
	then
		echo "quit"
	else 
		echo "unknown input"
		echo -n "Enter the item number: "
		readMenuNumber
	fi
}

showMenu
readMenuNumber