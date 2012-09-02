if [ $USER = "root" ]; then
  gcc -DHAVE_EXPAT_CONFIG_H -DHAVE_MEMMOVE -Iinclude -O0 -g3 -Wall src/*.c -o stripper
  cp stripper /usr/local/bin/
else
	echo "Need to be root. Did you use sudo?"
fi
