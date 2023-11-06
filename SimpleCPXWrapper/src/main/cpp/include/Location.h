//Location_h
#ifndef Location_H
#define Location_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct Location Location;

struct Location {
  double latitude;
  double longitude;
  double elevation;
  unsigned long long time;
  Location *next;
};

#ifdef __cplusplus
}
#endif

#endif
