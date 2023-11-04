#include "Location.h"

//Waypoint_h
#ifndef Waypoint_H
#define Waypoint_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct Waypoint Waypoint;

struct Waypoint {
  Location *location;
  char *name;
  char *desc;
  char *sym;
  //int d;
  Waypoint *next;
};

#ifdef __cplusplus
}
#endif

#endif // !Waypoint_H

