#### Install muttr and mstmp
```
#ubuntu/debian
apt install -y mutt msmtp
#centos
yum install -y mutt msmtp
```

#### Edit `/etc/Muttrc` and add content below
```
set charset = "utf-8"
set sendmail = "/usr/bin/msmtp"
set use_from = yes
set realname = 'your name'
set from = 'your email'
set envelope_from = yes
set crypt_use_gpgme = no
```

#### Config msmtp
Run `vi ~/.msmtprc` and add content below
```
account default
auth plain
host 'smtp server address'
from 'your email'
user 'your email'
password 'your email authrization code'
logfile ~/.msmtp.log
```

Modify msmtp configuration file's policy and create log file

```
chmod 600 .msmtprc
touch ~/.msmtp.log
```

#### Send mail
```
echo "content" | mutt -s "title" -e 'set content_type="text/html"' xxxxx@xxx.xxx
```
