gcc -DCOMMANDLINE -DHAVE_EXPAT_CONFIG_H -DHAVE_MEMMOVE -Iinclude -Iinclude/AESE -Iinclude/STIL -O0 -g3 -Wall src/STIL/*.c src/AESE/*.c src/*.c -o formatter
