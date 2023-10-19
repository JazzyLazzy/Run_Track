#include "Location.h"

//Track_Seg
#ifndef Track_Seg_H
#define Track_Seg_H

typedef struct {
  int elevation_gain;
  int elevation_loss;
} Elevation;

typedef struct {
  Location *locations;
  double distance;
  unsigned long long time;
  Elevation elevation;
  struct Track_Seg *next;
} Track_Seg;


#endif // !Track_Seg_H
