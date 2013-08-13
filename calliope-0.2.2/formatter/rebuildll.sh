if [ $USER = "root" ]; then
  if [ `uname` = "Darwin" ]; then
    LIBSUFFIX="dylib"
    JDKINC="/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers"
  else
    LIBSUFFIX="so"
    JDKINC="/usr/lib/jvm/java-7-openjdk-amd64/include"
  fi
  gcc -c -DHAVE_EXPAT_CONFIG_H -DHAVE_MEMMOVE -DJNI -I$JDKINC -Iinclude -Iinclude/STIL -Iinclude/AESE -O0 -Wall -g3 -fPIC src/*.c src/AESE/*.c src/STIL/*.c 
  gcc *.o -shared -o libAeseFormatter.$LIBSUFFIX
  mv libAeseFormatter.$LIBSUFFIX /usr/local/lib/
  rm *.o
else
	echo "Need to be root. Did you use sudo?"
fi

