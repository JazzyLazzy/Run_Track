//Location_h
#ifndef Location_H
#define Location_H

typedef struct {
  double latitude;
  double longitude;
  int elevation;
  unsigned long long time;
  struct Location *next;
} Location;

#endif
