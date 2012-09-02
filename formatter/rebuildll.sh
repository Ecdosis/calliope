if [ $USER = "root" ]; then
  if [ `uname` = "Darwin" ]; then
    LIBSUFFIX="dylib"
    JDKINC="/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers"
  else
    LIBSUFFIX="so"
    JDKINC="/usr/lib/jvm/java-6-openjdk/include"
  fi
  gcc -c -DHAVE_EXPAT_CONFIG_H -DHAVE_MEMMOVE -DJNI -I$JDKINC -Iinclude -Iinclude/STIL -Iinclude/HRIT -O0 -Wall -g3 -fPIC src/*.c src/HRIT/*.c src/STIL/*.c 
  gcc *.o -shared -o libHritFormatter.$LIBSUFFIX
  cp libHritFormatter.$LIBSUFFIX /usr/local/lib
else
	echo "Need to be root. Did you use sudo?"
fi

