#define PREFERENCES_CODE 0
#define PREFERENCE_STATE_CODE 0
#define EFFECTS_CODE 1
#define EFFECT_FILL_CODE 0
#define EFFECT_DRAW_CODE 2
#define EFFECT_SINUS_FILL_CODE 3
#define EFFECT_RAIN_CODE 4
#define EFFECT_RAINBOW_CODE 5

#define GAMES_CODE 2
#define GAME_SNAKE_CODE 0

#define EOL ';'
#define DELIMETER ','

#define BLUETOOTH_SPEED 115200

#define LAYERS 8
#define ROWS 8
#define COLUMNS 8

// continues effects/games
#define EFFECT_NONE 0
#define EFFECT_DRAW 2
#define EFFECT_SINUS 3
#define EFFECT_RAIN 4
#define EFFECT_RAINBOW 5
#define GAME_SNAKE 6

#define SNAKE_START 4
#define D_UP 0
#define D_DOWN 1
#define D_LEFT 2
#define D_RIGHT 3
#define D_FORWARD 4
#define D_BACKWARD 5

// pins
#define LAYER1_PIN 9
#define LAYER2_PIN 8
#define LAYER3_PIN 7
#define LAYER4_PIN 6
#define LAYER5_PIN 5
#define LAYER6_PIN 4
#define LAYER7_PIN 3
#define LAYER8_PIN 2

#include <ledMatrix.h>

bool state = true;
uint8_t currentEffect = EFFECT_NONE;
uint32_t millTimer;

ledMatrix<ROWS, COLUMNS, LAYER1_PIN> layer1;
ledMatrix<ROWS, COLUMNS, LAYER2_PIN> layer2;
ledMatrix<ROWS, COLUMNS, LAYER3_PIN> layer3;
ledMatrix<ROWS, COLUMNS, LAYER4_PIN> layer4;
ledMatrix<ROWS, COLUMNS, LAYER5_PIN> layer5;
ledMatrix<ROWS, COLUMNS, LAYER6_PIN> layer6;
ledMatrix<ROWS, COLUMNS, LAYER7_PIN> layer7;
ledMatrix<ROWS, COLUMNS, LAYER8_PIN> layer8;

void clear()
{
  layer1.fill(0);
  layer2.fill(0);
  layer3.fill(0);
  layer4.fill(0);
  layer5.fill(0);
  layer6.fill(0);
  layer7.fill(0);
  layer8.fill(0);
}

uint16_t effectColor;
boolean loading = true;
void fillColor()
{
  layer1.fill(effectColor);
  layer2.fill(effectColor);
  layer3.fill(effectColor);
  layer4.fill(effectColor);
  layer5.fill(effectColor);
  layer6.fill(effectColor);
  layer7.fill(effectColor);
  layer8.fill(effectColor);
  draw();
}

void drawEffect(uint8_t layer, uint8_t row, uint8_t column, uint16_t color)
{
  if (loading && currentEffect != EFFECT_DRAW)
  {
    clear();
    draw();
    currentEffect = EFFECT_DRAW;
    loading = false;
  }
  switch (layer)
  {
  case 0 ... 7:
    setPixel(layer, row, column, color);
    break;
  case 9:
    switch (column)
    {
    case 0:
      layer1.fill(0);
      break;
    case 1:
      layer2.fill(0);
      break;
    case 2:
      layer3.fill(0);
      break;
    case 3:
      layer4.fill(0);
      break;
    case 4:
      layer5.fill(0);
      break;
    case 5:
      layer6.fill(0);
      break;
    case 6:
      layer7.fill(0);
      break;
    case 7:
      layer8.fill(0);
      break;
    }
  }
  draw();
}

void setup()
{
  Serial.begin(BLUETOOTH_SPEED);
  off();
}

void loop()
{
  for (;;)
  {
    if (Serial.available())
    {
      parseCommand();
    }

    switch (currentEffect)
    {
    case EFFECT_RAINBOW:
    {
      rainbow();
      break;
    }
    case EFFECT_SINUS:
    {
      sinusFill();
      break;
    }
    case EFFECT_RAIN:
    {
      rain();
      break;
    }
    case GAME_SNAKE:
    {
      snake();
      break;
    }
    }
  }
}

void sendState()
{
  char *temp = new char[7];
  temp[0] = PREFERENCES_CODE + '0';
  temp[1] = DELIMETER;
  temp[2] = PREFERENCE_STATE_CODE + '0';
  temp[3] = DELIMETER;
  temp[4] = state ? '1' : '0';
  temp[5] = EOL;
  temp[6] = '\0';
  Serial.write(temp);
  delete[] temp;
}

void setPixel(int layer, int row, int column, uint16_t color)
{
  switch (layer)
  {
  case 0:
    layer1.set(column, row, color);
    break;
  case 1:
    layer2.set(column, row, color);
    break;
  case 2:
    layer3.set(column, row, color);
    break;
  case 3:
    layer4.set(column, row, color);
    break;
  case 4:
    layer5.set(column, row, color);
    break;
  case 5:
    layer6.set(column, row, color);
    break;
  case 6:
    layer7.set(column, row, color);
    break;
  case 7:
    layer8.set(column, row, color);
    break;
  }
}

void sendScore(uint8_t score, boolean isGameOver = false)
{
  uint8_t size;
  switch (score)
  {
  case 0 ... 9:
    size = 1;
    break;
  case 10 ... 99:
    size = 2;
    break;
  case 100 ... 255:
    size = 3;
    break;
  }
  char *temp = new char[size + 8];
  temp[0] = GAMES_CODE + '0';
  temp[1] = DELIMETER;
  temp[2] = GAME_SNAKE_CODE + '0';
  temp[3] = DELIMETER;
  temp[4] = isGameOver ? '0' : '1';
  temp[5] = DELIMETER;
  for (int i = size - 1; i >= 0; --i)
  {
    temp[i + 6] = (score % 10) + '0';
    score /= 10;
  }
  temp[size + 6] = EOL;
  temp[size + 7] = '\0';
  Serial.write(temp);
  delete[] temp;
}

int8_t snakeArray[SNAKE_START + 100][3];
uint8_t snakeLength;
bool generateApple;
uint8_t apple[3];
enum direction
{
  UP,
  DOWN,
  LEFT,
  RIGHT,
  FORWARD,
  BACKWARD,
  STOP
} dir;
uint16_t appleColor;
uint16_t headColor;
uint16_t snakeColor;
uint16_t speed = 8000;

void snake()
{
  if (loading)
  {
    clear();
    draw();
    loading = false;
    dir = RIGHT;
    currentEffect = GAME_SNAKE;
    snakeLength = SNAKE_START;
    generateApple = true;
    for (uint8_t i = 0; i < SNAKE_START; i++)
    {
      snakeArray[i][0] = SNAKE_START - i - 1;
      snakeArray[i][1] = 0;
      snakeArray[i][2] = 7;
      setPixel(7, 0, SNAKE_START - i - 1, (i == 0 ? headColor : snakeColor));
    }
    millTimer = millis();
    draw();
  }

  while (generateApple)
  {
    apple[0] = random(0, 8);
    apple[1] = random(0, 8);
    apple[2] = random(0, 8);
    for (uint8_t i = 0; i < snakeLength; i++)
    {
      if (snakeArray[i][0] == apple[0] && snakeArray[i][1] == apple[1] && snakeArray[i][2] == apple[2])
        break;
      if (i == snakeLength - 1)
      {
        generateApple = false;
        setPixel(apple[2], apple[1], apple[0], appleColor);
        draw();
      }
    }
  }

  if (millis() - millTimer >= speed)
  {
    millTimer = millis();
    uint8_t xHeadPrev = snakeArray[0][0];
    uint8_t yHeadPrev = snakeArray[0][1];
    uint8_t zHeadPrev = snakeArray[0][2];
    switch (dir)
    {
    case UP:
      snakeArray[0][2]++;
      break;
    case DOWN:
      snakeArray[0][2]--;
      break;
    case LEFT:
      snakeArray[0][0]--;
      break;
    case RIGHT:
      snakeArray[0][0]++;
      break;
    case FORWARD:
      snakeArray[0][1]++;
      break;
    case BACKWARD:
      snakeArray[0][1]--;
      break;
    }

    // check apple
    if (snakeArray[0][0] == apple[0] &&
        snakeArray[0][1] == apple[1] &&
        snakeArray[0][2] == apple[2])
    {
      snakeLength++;
      for (uint8_t i = snakeLength - 1; i > 1; i--)
      {
        snakeArray[i][0] = snakeArray[i - 1][0];
        snakeArray[i][1] = snakeArray[i - 1][1];
        snakeArray[i][2] = snakeArray[i - 1][2];
      }

      snakeArray[1][0] = xHeadPrev;
      snakeArray[1][1] = yHeadPrev;
      snakeArray[1][2] = zHeadPrev;
      generateApple = true;
    }
    else
    {
      setPixel(snakeArray[snakeLength - 1][2], snakeArray[snakeLength - 1][1], snakeArray[snakeLength - 1][0], 0);
      for (uint8_t i = snakeLength - 1; i > 1; i--)
      {
        snakeArray[i][0] = snakeArray[i - 1][0];
        snakeArray[i][1] = snakeArray[i - 1][1];
        snakeArray[i][2] = snakeArray[i - 1][2];
      }
      snakeArray[1][0] = xHeadPrev;
      snakeArray[1][1] = yHeadPrev;
      snakeArray[1][2] = zHeadPrev;
    }

    // check collide with walls
    if (snakeArray[0][0] < 0 || snakeArray[0][0] > 7 ||
        snakeArray[0][1] < 0 || snakeArray[0][1] > 7 ||
        snakeArray[0][2] < 0 || snakeArray[0][2] > 7)
    {
      currentEffect = EFFECT_NONE;
      sendScore(snakeLength - SNAKE_START, true);
      loading = true;
      off();
      delay(500);
      draw();
      delay(500);
      off();
      delay(500);
      draw();
      delay(500);
      off();
      delay(500);
      draw();
      return;
    }

    // check self collide
    for (uint8_t i = 2; i < snakeLength; i++)
    {
      if (snakeArray[0][0] == snakeArray[i][0] &&
          snakeArray[0][1] == snakeArray[i][1] &&
          snakeArray[0][2] == snakeArray[i][2])
      {
        currentEffect = EFFECT_NONE;
        sendScore(snakeLength - SNAKE_START, true);
        loading = true;
        off();
        delay(500);
        draw();
        delay(500);
        off();
        delay(500);
        draw();
        delay(500);
        off();
        delay(500);
        draw();
        return;
      }
    }
    
    // win
    if (snakeLength == SNAKE_START + 100)
    {
      currentEffect = EFFECT_NONE;
      sendScore(snakeLength - SNAKE_START, true);
      loading = true;
      off();
      delay(500);
      draw();
      delay(500);
      off();
      delay(500);
      draw();
      delay(500);
      off();
      delay(500);
      draw();
      return;
    }

    setPixel(snakeArray[0][2], snakeArray[0][1], snakeArray[0][0], headColor);
    setPixel(snakeArray[1][2], snakeArray[1][1], snakeArray[1][0], snakeColor);
    sendScore(snakeLength - SNAKE_START);
    draw();
  }
}

int8_t pos = 0;
void sinusFill()
{
  if (loading)
  {
    currentEffect = EFFECT_SINUS;
    clear();
    loading = false;
    millTimer = millis();
  }
  if (millis() - millTimer >= speed)
  {
    millTimer = millis();
    clear();
    if (++pos > 10)
      pos = 0;
    for (uint8_t i = 0; i < 8; i++)
    {
      for (uint8_t j = 0; j < 8; j++)
      {
        int8_t sinZ = 4 + ((float)sin((float)(i + pos) / 2) * 3);
        for (uint8_t y = 0; y < sinZ; y++)
        {
          setPixel(y, j, i, effectColor);
        }
      }
    }
    draw();
  }
}

void rain()
{
  if (loading)
  {
    currentEffect = EFFECT_RAIN;
    clear();
    loading = false;
    millTimer = millis();
  }
  if (millis() - millTimer >= speed)
  {
    millTimer = millis();
    for (int i = 0; i < COLUMNS * ROWS; i++)
    {
      layer1.leds[i] = layer2.leds[i];
      layer2.leds[i] = layer3.leds[i];
      layer3.leds[i] = layer4.leds[i];
      layer4.leds[i] = layer5.leds[i];
      layer5.leds[i] = layer6.leds[i];
      layer6.leds[i] = layer7.leds[i];
      layer7.leds[i] = layer8.leds[i];
      layer8.leds[i] = 0;
    }

    uint8_t numDrops = random(0, 5);
    for (uint8_t i = 0; i < numDrops; i++)
    {
      setPixel(7, random(0, 8), random(0, 8), effectColor);
    }
    draw();
  }
}

void rainbow()
{
  if (loading)
  {
    currentEffect = EFFECT_RAINBOW;
    loading = false;
    millTimer = millis();
    clear();
    draw();
    pos = 0;
  }
  if (millis() - millTimer > speed)
  {
    millTimer = millis();
    uint16_t color = getHSV(pos, 255, 255);
    if (pos >= 255)
      pos = 0;
    pos += 1;
    layer1.fill(color);
    layer2.fill(color);
    layer3.fill(color);
    layer4.fill(color);
    layer5.fill(color);
    layer6.fill(color);
    layer7.fill(color);
    layer8.fill(color);
  }
  draw();
}

void parseCommand()
{
  char buf[44];
  uint8_t amount = Serial.readBytesUntil(EOL, buf, 44);
  buf[amount] = 0;
  uint8_t data[12];
  uint8_t count = 0;
  char *offset = buf;

  while (true)
  {
    data[count++] = atoi(offset);
    offset = strchr(offset, DELIMETER);
    if (offset)
      offset++;
    else
      break;
  }

  // getData
  if (count == 2)
  {
    switch (data[0])
    {
    case PREFERENCES_CODE:
      switch (data[1])
      {
      case PREFERENCE_STATE_CODE:
        sendState();
        break;
      }
      break;
    }
  }
  else // setData
  {
    switch (data[0])
    {
    case PREFERENCES_CODE:
      switch (data[1])
      {
      case PREFERENCE_STATE_CODE:
        state = data[2];
        if (state)
          draw();
        else
          off();
        break;
      default:
        return;
      }
      break;
    case EFFECTS_CODE:
      switch (data[1])
      {
      case EFFECT_FILL_CODE:
        loading = true;
        effectColor = getRGB(data[2], data[3], data[4]);
        fillColor();
        break;
      case EFFECT_RAINBOW_CODE:
        loading = true;
        speed = 510 - (data[2] << 1);
        rainbow();
        break;
      case EFFECT_SINUS_FILL_CODE:
        effectColor = getRGB(data[2], data[3], data[4]);
        speed = 127 - (data[5] >> 1);
        loading = true;
        sinusFill();
        break;
      case EFFECT_RAIN_CODE:
        effectColor = getRGB(data[2], data[3], data[4]);
        speed = 255 - data[5];
        loading = true;
        rain();
        break;
      case EFFECT_DRAW_CODE:
        loading = true;
        drawEffect(data[2], data[4], data[3], getRGB(data[5], data[6], data[7]));
      default:
        return;
      }
      break;
    case GAMES_CODE:
      switch (data[1])
      {
      case GAME_SNAKE_CODE:
      {
        if (count == 3)
        {
          switch (data[2])
          {
          case D_UP:
            if (dir != DOWN)
            {
              dir = UP;
            }
            break;
          case D_DOWN:
            if (dir != UP)
            {
              dir = DOWN;
            }
            break;
          case D_LEFT:
            if (dir != RIGHT)
            {
              dir = LEFT;
            }
            break;
          case D_RIGHT:
            if (dir != LEFT)
            {
              dir = RIGHT;
            }
            break;
          case D_BACKWARD:
            if (dir != FORWARD)
            {
              dir = BACKWARD;
            }
            break;
          case D_FORWARD:
            if (dir != BACKWARD)
            {
              dir = FORWARD;
            }
            break;
          }
          break;
        }
        loading = true;
        headColor = getRGB(data[2], data[3], data[4]);
        snakeColor = getRGB(data[5], data[6], data[7]);
        appleColor = getRGB(data[8], data[9], data[10]);
        switch (data[11])
        {
        case 0:
          speed = 1000;
          break;
        case 1:
          speed = 700;
          break;
        case 2:
          speed = 500;
          break;
        }
        snake();
        break;
      }
      default:
        return;
      }
      break;
    default:
      return;
    }
  }
}

void off()
{
  layer1.show(0);
  layer2.show(0);
  layer3.show(0);
  layer4.show(0);
  layer5.show(0);
  layer6.show(0);
  layer7.show(0);
  layer8.show(0);
}

void draw()
{
  if (!state)
    return;
  layer1.show();
  layer2.show();
  layer3.show();
  layer4.show();
  layer5.show();
  layer6.show();
  layer7.show();
  layer8.show();
}