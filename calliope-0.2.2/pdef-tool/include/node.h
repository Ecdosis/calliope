/* 
 * File:   node.h
 * Author: desmond
 *
 * Created on May 29, 2012, 5:00 PM
 */

#ifndef NODE_H
#define	NODE_H
#define NODE_NAME "name"
#define NODE_UPDATED "updated"
#define NODE_CHILDREN "children"
#define NODE_CORFORM "corform"
#define DEFAULT "default"

#ifdef	__cplusplus
extern "C" {
#endif
typedef struct node_struct node;
node *node_create( char *name, long last_update );
node *node_create_bare();
int node_set_name( node *n, char *name );
void node_set_updated( node *n, long value );
void node_dispose( node *n );
void node_add_child( node *parent, node *child );
int node_save( node *n, FILE *dst );


#ifdef	__cplusplus
}
#endif

#endif	/* NODE_H */

