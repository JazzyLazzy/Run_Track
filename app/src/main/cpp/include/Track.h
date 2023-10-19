#include "Track_Seg.h"

//Track_H
#ifndef Track_H
#define Track_H

typedef struct{
  char *name;
  Track_Seg *track_segs;
  struct Track *next;
} Track;

#endif
