#ifndef _ledMatrix_h
#define _ledMatrix_h

#include <Arduino.h>

// convert from 16-bit color value to RGB values
#define getR(x) (((x) & 0b1111100000000000) >> 11)
#define getG(x) (((x) & 0b0000011111000000) >> 6)
#define getB(x) (((x) & 0b0000000000111110) >> 1)

// convert RGB values to 16-bit color value
#define getRGB(r, g, b) ((((r) & 0b11111000) << 8) | (((g) & 0b11111000) << 3) | (((b) & 0b11111000) >> 2))
// convert HSV values to 16-bit color value
uint16_t getHSV(uint8_t h, uint8_t s, uint8_t v)
{
    float r, g, b;

    float H = h / 255.0;
    float S = s / 255.0;
    float V = v / 255.0;

    int i = int(H * 6);
    float f = H * 6 - i;
    float p = V * (1 - S);
    float q = V * (1 - f * S);
    float t = V * (1 - (1 - f) * S);

    switch (i % 6)
    {
    case 0:
        r = V, g = t, b = p;
        break;
    case 1:
        r = q, g = V, b = p;
        break;
    case 2:
        r = p, g = V, b = t;
        break;
    case 3:
        r = p, g = q, b = V;
        break;
    case 4:
        r = t, g = p, b = V;
        break;
    case 5:
        r = V, g = p, b = q;
        break;
    }
    r *= 255.0;
    g *= 255.0;
    b *= 255.0;
    return getRGB((byte)r, (byte)g, (byte)b);
}

// led matrix class
template <uint8_t width, uint8_t height, int8_t pin>
class ledMatrix
{
public:
    uint16_t leds[width * height]; // array to store color values for each led in matrix 

    // prepare pin
    void init()
    {
        pin_mask = digitalPinToBitMask(pin);                  // get bit mask of pin
        pin_port = portOutputRegister(digitalPinToPort(pin)); // get register of port states
        port_ddr = portModeRegister(digitalPinToPort(pin));   // get register of port I/O configurations
        *port_ddr |= pin_mask;                                // set pin for output (0 - in, 1 - out)
    }

    // constructor
    ledMatrix()
    {
        init();
    }

    // fill all leds with color
    void fill(uint16_t color)
    {
        for (uint8_t i = 0; i < width * height; i++)
            leds[i] = color;
    }

    // get number of pixel in array by its coordinates
    uint8_t getNumberOfPixel(uint8_t x, uint8_t y)
    {
        if (y % 2)
            return (height * width - y * width - x - 1);    // if even row 
        else
            return (height * width - y * width - width + x);// if odd row
    }

    // set color for pixel
    void set(uint8_t x, uint8_t y, uint16_t color)
    {
        if (x < 0 || y < 0 || x >= width || y >= height) // check if coordinates are out of range
            return;
        leds[getNumberOfPixel(x, y)] = color;
    }

    // get color by coordinates
    uint16_t get(uint8_t x, uint8_t y)
    {
        return leds[getNumberOfPixel(x, y)];
    }

    // show all pixels
    void show()
    {
        mask_h = pin_mask | *pin_port;  // mask of high level
        mask_l = ~pin_mask & *pin_port; // mask of low level
        for (uint8_t i = 0; i < width * height; i++)
            send(leds[i]);  // send led 
    }

    void show(uint16_t color)
    {
        mask_h = pin_mask | *pin_port;  // mask of high level
        mask_l = ~pin_mask & *pin_port; // mask of low level
        for (uint8_t i = 0; i < width * height; i++)
            send(color); // send led
    }

    // send led color
    void send(uint16_t color)
    {
        // convert from 16-bit color value to RGB
        uint8_t data[3];
        data[0] = getR(color);
        data[1] = getG(color);
        data[2] = getB(color);

        cli(); // turn off interrupts for sending
        // send RGB
        for (uint8_t i = 0; i < 3; i++)
            sendValue(data[i]);
        sei(); // turn oт interrupts
    }

    // send color value
    void sendValue(byte data)
    {
        asm volatile(
            "LDI r24, 8         \n\t" // write to register r24 value 8 - loop counter
            "LOOP_START:        \n\t" // label of loop start
            "ST X, %[SET_H]     \n\t" // set high signal on pin

            "SBRS %[DATA], 7    \n\t" // if last bit is equal 1 skip setting pin to low
            "ST X, %[SET_L]     \n\t" // set low signal on pin --4 ticks = 250ns
            "LSL  %[DATA]       \n\t" // shift data by 1

            // delay 4 ticks: 1 cycle of 3 ticks, write value of 1 tick
            "LDI r25, 4         \n\t" // write to register r25 value 1
            "DELAY_LOOP:        \n\t" // label of delay loop
            "DEC r25            \n\t" // decrement - 1 tick
            "BRNE DELAY_LOOP    \n\t" // return to label - 2 ticks

            "ST X, %[SET_L]     \n\t" // set low signal on pin --9tics = 562.5ns
            "DEC r24            \n\t" // decrement loop counter
            "BRNE LOOP_START    \n\t" // return to loop start
            :                         // no output variabless
            : [DATA] "r"(data),       // input variables
              [SET_H] "r"(mask_h),
              [SET_L] "r"(mask_l),
              "x"(pin_port)           // write to X port for data output
            : "r24", "r25");          // used registers
    }

private:
    volatile uint8_t *pin_port, *port_ddr;
    uint8_t pin_mask;
    uint8_t mask_h, mask_l;
};
#endif