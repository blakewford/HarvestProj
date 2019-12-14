#include "jni.h"

#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <ctype.h>
#include "kann.h"

#define MAX_FIELDS 64

int kann(int latitude, int longitude, uint8_t* model)
{
    kad_trap_fe();
    kann_srand(11);
    kann_t *ann = kann_sideload(model);

    uint64_t x[MAX_FIELDS], y = 0;
    int k, n_in;
    n_in = kann_dim_in(ann);

    x[0] = latitude;
    x[1] = longitude;

    float x1[MAX_FIELDS];
    kann_rnn_start(ann);
    for (k = 0, y = 0; k < 64; ++k) {
        const float *y1;
        for (int i = 0; i < n_in; ++i)
            x1[i] = (float) (x[i] >> k & 1);
        y1 = kann_apply1(ann, x1);
        if (y1[1] > y1[0]) y |= 1ULL << k;
    }
    kann_rnn_end(ann);
    kann_delete(ann);

    return (int) y;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_radical_harvest_KannNative_run(JNIEnv* env, jobject, jint latitude, jint longitude, jbyteArray model)
{
    jbyte* buffer = env->GetByteArrayElements(model, nullptr);
    jsize length = env->GetArrayLength(model);

    int32_t value = kann(latitude, longitude, (uint8_t*)buffer);

    env->ReleaseByteArrayElements(model, buffer, 0);

    return value;
}

int main(int argc, char** argv)
{
    FILE* executable = fopen("impervious.kan","rb");
    if(executable)
    {
        fseek(executable, 0, SEEK_END);
        int32_t size = ftell(executable);
        rewind(executable);
        uint8_t* binary = (uint8_t*)malloc(size);
        size_t read = fread(binary, 1, size, executable);
        if(read != size) return -1;
        fclose(executable); 

        printf("%d\n", kann(400958, 737471, binary));
        printf("%d\n", kann(303414, 977277, binary));
        printf("%d\n", kann(303433, 977316, binary));        
    }
   
    return 0;
}
