%module hrit
%{
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "attribute.h"
#include "hashmap.h"
#include "annotation.h"
#include "range.h"
#include "range_array.h"
#include "hashset.h"
#include "formatter.h"
#include "hrit_formatter.h"
#include "HRIT/HRIT.h"
#include "STIL/STIL.h"

%}

%newobject hrit_formatter;
%include "include/hrit_formatter.h"
            
