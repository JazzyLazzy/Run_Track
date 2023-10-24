#include "Location.h"

//Track_Seg
#ifndef Track_Seg_H
#define Track_Seg_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
  int elevation_gain;
  int elevation_loss;
} Elevation;

typedef struct Track_Seg Track_Seg;

struct Track_Seg {
    Location *locations;
    double distance;
    unsigned long long time;
    Elevation elevation;
    Track_Seg *next;
};

#ifdef __cplusplus
}
#endif


#endif // !Track_Seg_H
