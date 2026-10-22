#version 330

// Visor UI uber shader. Every quad describes one shape; the mode lives in vExtra.x:
//   0 rounded rectangle fill (vShape.w > 0 turns it into a soft shadow with that blur radius)
//   1 rounded rectangle border (vShape.w = border width, drawn inside the shape)
//   2 SDF glyph from the font atlas (vExtra.y = weight bias in atlas px, vExtra.z = softness)
//   3 image with rounded-corner mask (vExtra.y = 1 for premultiplied alpha)
//   4 animated "aurora" gradient (vExtra.y = seed, vExtra.z = palette)
//   5 ring / arc (vShape.x = radius, vShape.y = thickness, vExtra.y = start, vExtra.z = sweep, radians)
//   6 triangle pointing right ("play"), filling the shape's box, vShape.z = corner radius

layout(std140) uniform VisorUi {
    vec4 Atlas;   // atlas width, atlas height, SDF range (atlas px), time (seconds)
    vec4 Params;  // x = text contrast boost, y = 1 when back faces are drawn black (world panels)
};

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec4 vColor;
in vec2 vUv;
in vec2 vLocal;
flat in vec4 vShape;
flat in vec4 vExtra;

out vec4 fragColor;

float sdRoundRect(vec2 p, vec2 halfSize, float r) {
    r = min(r, min(halfSize.x, halfSize.y));
    vec2 q = abs(p) - halfSize + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

float sdTriangle(vec2 p, vec2 p0, vec2 p1, vec2 p2) {
    vec2 e0 = p1 - p0;
    vec2 e1 = p2 - p1;
    vec2 e2 = p0 - p2;
    vec2 v0 = p - p0;
    vec2 v1 = p - p1;
    vec2 v2 = p - p2;
    vec2 pq0 = v0 - e0 * clamp(dot(v0, e0) / dot(e0, e0), 0.0, 1.0);
    vec2 pq1 = v1 - e1 * clamp(dot(v1, e1) / dot(e1, e1), 0.0, 1.0);
    vec2 pq2 = v2 - e2 * clamp(dot(v2, e2) / dot(e2, e2), 0.0, 1.0);
    float s = sign(e0.x * e2.y - e0.y * e2.x);
    vec2 d = min(min(vec2(dot(pq0, pq0), s * (v0.x * e0.y - v0.y * e0.x)),
                     vec2(dot(pq1, pq1), s * (v1.x * e1.y - v1.y * e1.x))),
                     vec2(dot(pq2, pq2), s * (v2.x * e2.y - v2.y * e2.x)));
    return -sqrt(d.x) * sign(d.y);
}

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec3 palette(int index, int slot) {
    // Three stops per palette: deep, mid, highlight.
    vec3 p[24] = vec3[24](
        vec3(0.035, 0.114, 0.345), vec3(0.090, 0.380, 0.980), vec3(0.470, 0.840, 1.000),  // 0 ocean
        vec3(0.180, 0.070, 0.420), vec3(0.760, 0.330, 0.560), vec3(1.000, 0.640, 0.470),  // 1 dusk
        vec3(0.200, 0.020, 0.560), vec3(0.560, 0.180, 0.890), vec3(1.000, 0.360, 0.740),  // 2 magenta
        vec3(0.030, 0.180, 0.220), vec3(0.120, 0.720, 0.560), vec3(0.720, 0.980, 0.820),  // 3 emerald
        vec3(0.450, 0.090, 0.120), vec3(0.980, 0.360, 0.330), vec3(1.000, 0.780, 0.450),  // 4 ember
        vec3(0.000, 0.200, 0.450), vec3(0.000, 0.560, 0.950), vec3(0.430, 0.900, 1.000),  // 5 cyan
        vec3(0.300, 0.040, 0.060), vec3(0.880, 0.120, 0.230), vec3(1.000, 0.520, 0.560),  // 6 crimson
        vec3(0.050, 0.070, 0.140), vec3(0.150, 0.230, 0.400), vec3(0.420, 0.510, 0.980)   // 7 night
    );
    return p[clamp(index, 0, 7) * 3 + slot];
}

vec3 aurora(vec2 uv, vec2 aspect, float time, float seed, int pal) {
    vec3 deep = palette(pal, 0);
    vec3 mid = palette(pal, 1);
    vec3 high = palette(pal, 2);
    vec2 p = uv * aspect;
    float t = time * 0.18 + seed * 7.13;
    vec3 col = mix(deep, mid * 0.75, smoothstep(0.0, 1.3, uv.x * 0.8 + (1.0 - uv.y) * 0.5));
    vec2 c1 = vec2(0.25 + 0.20 * sin(t * 1.1 + seed), 0.30 + 0.25 * cos(t * 0.9)) * aspect;
    vec2 c2 = vec2(0.75 + 0.18 * cos(t * 0.8 + 1.7), 0.65 + 0.22 * sin(t * 1.3 + seed * 2.0)) * aspect;
    vec2 c3 = vec2(0.55 + 0.30 * sin(t * 0.6 + 3.1), 0.15 + 0.20 * cos(t * 1.7 + 0.4)) * aspect;
    float s = max(aspect.x, aspect.y);
    col += mid * 0.85 * exp(-pow(length(p - c1) / (0.42 * s), 2.0));
    col += high * 0.65 * exp(-pow(length(p - c2) / (0.30 * s), 2.0));
    col += mix(mid, high, 0.5) * 0.45 * exp(-pow(length(p - c3) / (0.25 * s), 2.0));
    // Soft diagonal light bands.
    float band = sin((uv.x * 1.6 - uv.y * 0.9) * 6.2831 + t * 2.0);
    col += high * 0.06 * smoothstep(0.6, 1.0, band);
    // Gentle vignette towards the bottom so text stays readable.
    col *= mix(1.0, 0.55, smoothstep(0.35, 1.0, uv.y));
    return col;
}

void main() {
    int mode = int(vExtra.x + 0.5);
    vec4 color = vColor;
    vec2 fw = fwidth(vLocal);
    float aa = max(0.5 * (fw.x + fw.y), 1e-4);
    float alpha = 1.0;

    if (mode == 0) {
        float d = sdRoundRect(vLocal, vShape.xy, vShape.z);
        if (vShape.w > 0.0) {
            alpha = 1.0 - smoothstep(-vShape.w, vShape.w, d);
            alpha *= alpha;
        } else {
            alpha = clamp(0.5 - d / aa, 0.0, 1.0);
        }
    } else if (mode == 1) {
        float d = sdRoundRect(vLocal, vShape.xy, vShape.z);
        float outer = clamp(0.5 - d / aa, 0.0, 1.0);
        float inner = clamp(0.5 - (d + vShape.w) / aa, 0.0, 1.0);
        alpha = outer - inner;
    } else if (mode == 2) {
        float sd = texture(Sampler0, vUv).a;
        float distAtlas = (sd - 0.5) * 2.0 * Atlas.z + vExtra.y;
        if (vExtra.z > 0.0) {
            alpha = smoothstep(-vExtra.z, vExtra.z, distAtlas);
        } else {
            vec2 unitRange = vec2(2.0 * Atlas.z) / Atlas.xy;
            vec2 screenTexSize = vec2(1.0) / max(fwidth(vUv), vec2(1e-7));
            float screenPxRange = max(0.5 * dot(unitRange, screenTexSize), 1.0);
            alpha = clamp(screenPxRange * distAtlas / (2.0 * Atlas.z) + 0.5, 0.0, 1.0);
            alpha = pow(alpha, Params.x);
        }
    } else if (mode == 3) {
        vec4 texel = texture(Sampler1, vUv);
        if (vExtra.y > 0.5) {
            // Premultiplied source (an off-screen render): back to straight alpha.
            texel.rgb /= max(texel.a, 1e-4);
        }
        color *= texel;
        float d = sdRoundRect(vLocal, vShape.xy, vShape.z);
        alpha = clamp(0.5 - d / aa, 0.0, 1.0);
    } else if (mode == 4) {
        vec2 size = max(vShape.xy * 2.0, vec2(1.0));
        vec2 uv = vLocal / size + 0.5;
        vec2 aspect = size / max(size.x, size.y);
        color.rgb *= aurora(uv, aspect, Atlas.w, vExtra.y, int(vExtra.z + 0.5));
        float d = sdRoundRect(vLocal, vShape.xy, vShape.z);
        alpha = clamp(0.5 - d / aa, 0.0, 1.0);
    } else if (mode == 5) {
        float r = length(vLocal);
        float ring = abs(r - vShape.x) - vShape.y * 0.5;
        alpha = clamp(0.5 - ring / aa, 0.0, 1.0);
        float angle = atan(vLocal.x, -vLocal.y);
        if (angle < 0.0) {
            angle += 6.2831853;
        }
        float rel = mod(angle - vExtra.y + 12.566370, 6.2831853);
        float sweep = vExtra.z;
        float edge = r * min(rel, sweep - rel);
        alpha *= sweep >= 6.28 ? 1.0 : clamp(0.5 + edge / aa, 0.0, 1.0) * step(rel, sweep);
    } else if (mode == 6) {
        vec2 h = max(vShape.xy - vShape.z, vec2(0.0));
        float d = sdTriangle(vLocal, vec2(-h.x, -h.y), vec2(h.x, 0.0), vec2(-h.x, h.y)) - vShape.z;
        alpha = clamp(0.5 - d / aa, 0.0, 1.0);
    }

    if (Params.y > 0.5 && !gl_FrontFacing) {
        // Seen from behind a panel is plain black, like the back of a screen.
        color.rgb = vec3(0.010, 0.010, 0.012);
    }

    color.a *= alpha;
    if (color.a <= 0.001) {
        discard;
    }
    // Break up gradient banding on large dark surfaces.
    color.rgb += (hash12(gl_FragCoord.xy) - 0.5) / 255.0;
    fragColor = color;
}
