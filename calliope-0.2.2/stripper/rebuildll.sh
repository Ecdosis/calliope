if [ $USER = "root" ]; then
  if [ `uname` = "Darwin" ]; then
    LIBSUFFIX="dylib"
    JDKINC="/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers"
  else
    LIBSUFFIX="so"
    JDKINC=" /usr/lib/jvm/java-7-openjdk-amd64/include"
  fi
  gcc -c -DHAVE_EXPAT_CONFIG_H -DHAVE_MEMMOVE -DJNI -I$JDKINC -Iinclude -I../formatter/include -I../formatter/include/STIL -O0 -Wall -g3 -fPIC ../formatter/src/STIL/cJSON.c src/*.c  
  gcc *.o -shared -lexpat -laspell -o libAeseStripper.$LIBSUFFIX
  cp libAeseStripper.$LIBSUFFIX /usr/local/lib
else
	echo "Need to be root. Did you use sudo?"
fi

