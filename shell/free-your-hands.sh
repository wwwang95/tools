#!/bin/bash

function showMenu {
	echo "FREE YOUR HANDS:)"
	echo "-----------------------------"
	echo "1. Update Centos7 Kernel"
	echo "0. Quit"
	echo "-----------------------------"
	echo -n "Enter the item number: "
}

function updateCentos7Kernel {
	rm -rf /var/cache/yum
	rpm --import https://www.elrepo.org/RPM-GPG-KEY-elrepo.org
	rpm -Uvh http://www.elrepo.org/elrepo-release-7.0-2.el7.elrepo.noarch.rpm
	Latest_kernel_version=`yum --disablerepo="*" --enablerepo="elrepo-kernel" list available | sed -n '/kernel-ml/P' | sed -n '/x86_64/p' | awk '{print $2}' | uniq`".x86_64"
	Installed_kernel_version=`uname -r`
	if [ $Latest_kernel_version == $Installed_kernel_version ];
	then
		echo "latest kernel has been installed: $Installed_kernel_version"
	else
		echo "the installed kernel's version is: $Installed_kernel_version, latest kernel is available: $Latest_kernel_version"
		yum --enablerepo=elrepo-kernel --skip-broken install -y kernel-ml kernel-ml-devel kernel-ml-headers kernel-ml-tools
		echo "-----------------------------"
		rpm -q kernel-ml-$Latest_kernel_version | awk '{if(/^package.*is not installed$/){print "kernel-ml-'$Latest_kernel_version' installation failed"}else{print "kernel-ml-'$Latest_kernel_version' installation succeed"}}'
		rpm -q kernel-ml-devel-$Latest_kernel_version | awk '{if(/^package.*is not installed$/){print "kernel-ml-devel-'$Latest_kernel_version' installation failed"}else{print "kernel-ml-devel-'$Latest_kernel_version' installation succeed"}}'
		rpm -q kernel-ml-headers-$Latest_kernel_version | awk '{if(/^package.*is not installed$/){print "kernel-ml-headers-'$Latest_kernel_version' installation failed"}else{print "kernel-ml-headers-'$Latest_kernel_version' installation succeed"}}'
		rpm -q kernel-ml-tools-$Latest_kernel_version | awk '{if(/^package.*is not installed$/){print "kernel-ml-tools-'$Latest_kernel_version' installation failed"}else{print "kernel-ml-tools-'$Latest_kernel_version' installation succeed"}}'
		echo "-----------------------------"
		
		if [ `cat /etc/default/grub | sed -n '/GRUB_DEFAULT/p' | awk -F'=' '{print $2}'` != 0 ]
		then
			echo "modify grub configuration"
			NOW_STR=`date +"%Y%m%d%H%M%S"`
			cp /etc/default/grub /etc/default/grub-$NOW_STR
			sed -i '/GRUB_DEFAULT/s/^.*$/GRUB_DEFAULT=0/g' /etc/default/grub
		fi
		grub2-mkconfig -o /boot/grub2/grub.cfg
		
		if [ -z `lsmod | grep bbr | awk '{print $1}'` ]
		then
			echo 'net.core.default_qdisc=fq' >> /etc/sysctl.conf
			echo 'net.ipv4.tcp_congestion_control=bbr'>> /etc/sysctl.conf
			sysctl -p
		fi

		echo -n "Reboot this machine?(y/n):"
		read reboot
		if [ $reboot == y ]
		then
			reboot
		else
			echo "quit"
		fi
	fi
}

function readMenuNumber {
	read num
	if [ $num == 1 ];
	then 
		updateCentos7Kernel
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