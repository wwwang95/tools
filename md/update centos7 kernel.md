### 1. User elrepo repository
```
rpm --import https://www.elrepo.org/RPM-GPG-KEY-elrepo.org
rpm -Uvh http://www.elrepo.org/elrepo-release-7.0-2.el7.elrepo.noarch.rpm
```

### 2. List available packages about kernel
```
yum --disablerepo="*" --enablerepo="elrepo-kernel" list available
```

### 3. Install latest stable kernel
```
yum --enablerepo=elrepo-kernel install kernel-ml kernel-ml-devel kernel-ml-headers
```

### 4. Modify GRUB configuration
Edit ``/etc/default/grub`` file to set ``GRUB_DEFAULT=0``, then reconfig
```
grub2-mkconfig -o /boot/grub2/grub.cfg
```

### 5. Reboot and check
```
uname -sr
```

### 6. Enable google bbr
Kernel over/eqaul 4.9
```
echo 'net.core.default_qdisc=fq' | tee -a /etc/sysctl.conf
echo 'net.ipv4.tcp_congestion_control=bbr' |  tee -a /etc/sysctl.conf
sysctl -p
```

Check
```
lsmod | grep bbr
```

### 7. Clean yum cache
```
rm -rf /var/cache/yum
```

### 8. bash for centos7 64bit
```
yum install -y wget && wget -N --no-check-certificate https://github.com/wwwang95/tools/raw/master/shell/free-your-hands.sh && chmod +x free-your-hands.sh && bash free-your-hands.sh
```
