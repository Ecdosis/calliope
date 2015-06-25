#ifndef MESSAGE_H
#define MESSAGE_H

UChar *str2ustr( char *str, UChar *u_str, int limit );
void ustr2str( UChar *u_str, char *str, int limit );
UChar *u_strdup( UChar *ustr );
int u_atoi( UChar *u_str );
#endif
