#include "Waypoint.h"
#include "Track.h"

//GPX_H
#ifndef GPX_H
#define GPX_H

typedef struct {
  char *creator;
  char version[4];
  Waypoint *waypoints;
  Track *tracks;
} GPX;

#endif
