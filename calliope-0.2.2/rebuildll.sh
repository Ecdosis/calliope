if [ $USER = "root" ]; then
  if [ `uname` = "Darwin" ]; then
    LIBSUFFIX="dylib"
    JDKINC="/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers"
  else
    LIBSUFFIX="so"
    JDKINC="/usr/lib/jvm/java-6-openjdk-amd64/include"
  fi
  gcc -c -DJNI -Iinclude -I$JDKINC -O0 -Wall -g3 -fPIC src/*.c 
  gcc *.o -shared -L/usr/local/lib -laspell -o libAeseSpeller.$LIBSUFFIX
  mv libAeseSpeller.$LIBSUFFIX /usr/local/lib/
  rm *.o
else
	echo "Need to be root. Did you use sudo?"
fi

