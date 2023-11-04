#include "Track_Seg.h"

//Track_H
#ifndef Track_H
#define Track_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct Track Track;

struct Track{
  char *name;
  Track_Seg *track_segs;
  Track *next;
};

#ifdef __cplusplus
}
#endif

#endif
