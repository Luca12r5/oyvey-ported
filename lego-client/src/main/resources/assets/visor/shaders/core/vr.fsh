#version 330

// A VR video all around the player, instead of the world. For every pixel the view ray is
// turned into a direction relative to the video's forward direction, and the video frame is read
// as an equirectangular picture: a full sphere (360°), the front half of one (180°), or a big flat
// screen floating in a dark room ("flat", for normal videos and ads).

layout(std140) uniform VrData {
    vec4 Corner0;  // world direction of the view ray at the bottom left of the screen
    vec4 Corner1;  // bottom right
    vec4 Corner2;  // top left
    vec4 Corner3;  // top right
    vec4 Forward;  // the video's forward direction (horizontal)
    vec4 Right;    // the video's right direction (horizontal)
    vec4 Screen;   // width, height, time, opacity
    vec4 Format;   // x = projection (0 sphere, 1 half sphere, 2 flat), y = flat screen half width (tan), z = flat aspect, w = 1 when a frame is there
    vec4 Tex;      // frame width, height, 1 / width, 1 / height
    vec4 Glow;     // rgb = average colour of the frame, a = strength of the glow around a flat screen
};

uniform sampler2D Sampler0;

out vec4 fragColor;

const float PI = 3.14159265;

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

// Catmull-Rom filtering from nine bilinear reads: noticeably sharper than plain bilinear when the
// video is magnified, which it always is in VR.
vec3 sharp(vec2 uv) {
    vec2 size = Tex.xy;
    vec2 position = uv * size;
    vec2 center = floor(position - 0.5) + 0.5;
    vec2 f = position - center;
    vec2 w0 = f * (-0.5 + f * (1.0 - 0.5 * f));
    vec2 w1 = 1.0 + f * f * (-2.5 + 1.5 * f);
    vec2 w2 = f * (0.5 + f * (2.0 - 1.5 * f));
    vec2 w3 = f * f * (-0.5 + 0.5 * f);
    vec2 w12 = w1 + w2;
    vec2 p0 = (center - 1.0) * Tex.zw;
    vec2 p3 = (center + 2.0) * Tex.zw;
    vec2 p12 = (center + w2 / w12) * Tex.zw;
    vec3 c = texture(Sampler0, vec2(p0.x, p0.y)).rgb * (w0.x * w0.y)
        + texture(Sampler0, vec2(p12.x, p0.y)).rgb * (w12.x * w0.y)
        + texture(Sampler0, vec2(p3.x, p0.y)).rgb * (w3.x * w0.y)
        + texture(Sampler0, vec2(p0.x, p12.y)).rgb * (w0.x * w12.y)
        + texture(Sampler0, vec2(p12.x, p12.y)).rgb * (w12.x * w12.y)
        + texture(Sampler0, vec2(p3.x, p12.y)).rgb * (w3.x * w12.y)
        + texture(Sampler0, vec2(p0.x, p3.y)).rgb * (w0.x * w3.y)
        + texture(Sampler0, vec2(p12.x, p3.y)).rgb * (w12.x * w3.y)
        + texture(Sampler0, vec2(p3.x, p3.y)).rgb * (w3.x * w3.y);
    return max(c, vec3(0.0));
}

// Dark room around a flat screen or behind a half sphere: almost black, a little lighter ahead.
vec3 room(vec3 dir) {
    float ahead = max(dot(dir, Forward.xyz), 0.0);
    float floorShade = smoothstep(0.0, -0.6, dir.y);
    return vec3(0.016, 0.017, 0.020) + vec3(0.012, 0.013, 0.016) * ahead * ahead - vec3(0.008) * floorShade;
}

void main() {
    vec2 screen = gl_FragCoord.xy / Screen.xy;
    vec3 ray = mix(mix(Corner0.xyz, Corner1.xyz, screen.x), mix(Corner2.xyz, Corner3.xyz, screen.x), screen.y);
    vec3 dir = normalize(ray);
    float ahead = dot(dir, Forward.xyz);
    float side = dot(dir, Right.xyz);
    int projection = int(Format.x + 0.5);
    bool ready = Format.w > 0.5;
    vec3 color = room(dir);

    if (projection == 2) {
        // A screen one unit ahead, Format.y wide on each side of the centre.
        vec2 halfSize = vec2(Format.y, Format.y / max(Format.z, 0.01));
        if (ahead > 0.05) {
            vec2 p = vec2(side, dir.y) / ahead;
            vec2 uv = vec2(0.5 + p.x / (2.0 * halfSize.x), 0.5 - p.y / (2.0 * halfSize.y));
            vec2 outside = max(abs(p) - halfSize, 0.0);
            float away = length(outside) / halfSize.y;
            // Light of the picture spilling onto the dark room around it.
            color += Glow.rgb * Glow.a * exp(-away * 2.6) * 0.55;
            if (ready && uv.x >= 0.0 && uv.x <= 1.0 && uv.y >= 0.0 && uv.y <= 1.0) {
                vec2 corner = max(abs(p) - (halfSize - 0.025), 0.0);
                float mask = 1.0 - smoothstep(0.022, 0.026, length(corner));
                color = mix(color, sharp(uv), mask);
            }
        }
    } else if (ready) {
        float lon = atan(side, ahead);
        float lat = asin(clamp(dir.y, -1.0, 1.0));
        if (projection == 0) {
            color = sharp(vec2(0.5 + lon / (2.0 * PI), 0.5 - lat / PI));
        } else {
            float inside = 1.0 - smoothstep(PI * 0.5 - 0.03, PI * 0.5, abs(lon));
            if (inside > 0.0) {
                color = mix(color, sharp(vec2(clamp(0.5 + lon / PI, 0.0, 1.0), 0.5 - lat / PI)), inside);
            }
        }
    }

    // Break up banding in the dark parts.
    color += (hash12(gl_FragCoord.xy + fract(Screen.z) * 61.0) - 0.5) / 255.0;
    fragColor = vec4(color, Screen.w);
}
