#version 330

// "Ghost hands" in the style of headset hand tracking: a translucent dark fill with a crisp
// white outline. Each hand part (palm, fingers, forearm) is a smooth union of tapered capsules
// that were projected to screen space on the CPU. Parts arrive sorted back to front, so the
// outlines of fingers that pass in front of the palm stay visible while hidden edges vanish.

#define MAX_SEGMENTS 48
#define MAX_PARTS 14
#define MAX_DECOS 6

layout(std140) uniform HandData {
    vec4 Info;       // x = part count, y = deco count, z = screen height (px), w = global alpha
    vec4 Style;      // x = fill alpha, y = outline width (px), z = inner line width (px), w = glow strength
    vec4 FillColor;  // rgb = fill colour, a = inner line alpha
    vec4 LineColor;  // rgb = outline colour, a = glow radius (px)
    vec4 Seg[MAX_SEGMENTS];   // segment end points (ax, ay, bx, by), pixels, top-left origin
    vec4 SegR[MAX_SEGMENTS];  // (radius a, radius b, unused, unused)
    vec4 Part[MAX_PARTS];     // (first segment, segment count, smoothing, alpha)
    vec4 Deco[MAX_DECOS];     // decoration capsules drawn on top (ax, ay, bx, by)
    vec4 DecoR[MAX_DECOS];    // (radius a, radius b, alpha a, alpha b)
};

out vec4 fragColor;

float sdUnevenCapsule(vec2 p, vec2 pa, vec2 pb, float ra, float rb) {
    p -= pa;
    pb -= pa;
    float h = dot(pb, pb);
    if (h < 1e-3) {
        return length(p) - max(ra, rb);
    }
    vec2 q = vec2(dot(p, vec2(pb.y, -pb.x)), dot(p, pb)) / h;
    q.x = abs(q.x);
    float b = ra - rb;
    vec2 c = vec2(sqrt(max(h - b * b, 1e-4)), b);
    float k = c.x * q.y - c.y * q.x;
    float m = dot(c, q);
    float n = dot(q, q);
    if (k < 0.0) {
        return sqrt(h * n) - ra;
    } else if (k > c.x) {
        return sqrt(h * (n + 1.0 - 2.0 * q.y)) - rb;
    }
    return m - ra;
}

float smin(float a, float b, float k) {
    if (k <= 0.0) {
        return min(a, b);
    }
    float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
    return mix(b, a, h) - k * h * (1.0 - h);
}

float strokeInside(float d, float width) {
    float outer = clamp(0.5 - d, 0.0, 1.0);
    float inner = clamp(0.5 - (d + width), 0.0, 1.0);
    return outer - inner;
}

void main() {
    vec2 p = vec2(gl_FragCoord.x, Info.z - gl_FragCoord.y);
    int partCount = int(Info.x + 0.5);
    int decoCount = int(Info.y + 0.5);

    float unionD = 1e6;
    float coverFront = 0.0;
    float innerLines = 0.0;

    for (int pi = MAX_PARTS - 1; pi >= 0; pi--) {
        if (pi >= partCount) {
            continue;
        }
        vec4 part = Part[pi];
        int first = int(part.x + 0.5);
        int count = int(part.y + 0.5);
        float d = 1e6;
        for (int s = 0; s < 6; s++) {
            if (s >= count) {
                break;
            }
            int si = first + s;
            vec4 seg = Seg[si];
            vec4 rad = SegR[si];
            d = smin(d, sdUnevenCapsule(p, seg.xy, seg.zw, rad.x, rad.y), part.z);
        }
        float cover = clamp(0.5 - d, 0.0, 1.0) * part.w;
        innerLines = max(innerLines, strokeInside(d, Style.z) * (1.0 - coverFront) * part.w);
        coverFront = max(coverFront, cover);
        unionD = min(unionD, d);
    }

    float fillA = coverFront * Style.x;
    float outline = strokeInside(unionD, Style.y);
    float lineA = max(outline, innerLines * FillColor.a);
    float glow = Style.w * exp(-max(unionD, 0.0) / max(LineColor.a, 0.5)) * (1.0 - coverFront);

    // Composite: glow under fill, lines on top.
    vec3 color = LineColor.rgb;
    float a = glow;
    color = mix(color, FillColor.rgb, fillA / max(fillA + a * (1.0 - fillA), 1e-4));
    a = fillA + a * (1.0 - fillA);
    color = mix(color, LineColor.rgb, lineA / max(lineA + a * (1.0 - lineA), 1e-4));
    a = lineA + a * (1.0 - lineA);

    for (int di = 0; di < MAX_DECOS; di++) {
        if (di >= decoCount) {
            break;
        }
        vec4 seg = Deco[di];
        vec4 rad = DecoR[di];
        float d = sdUnevenCapsule(p, seg.xy, seg.zw, rad.x, rad.y);
        vec2 ab = seg.zw - seg.xy;
        float t = clamp(dot(p - seg.xy, ab) / max(dot(ab, ab), 1e-4), 0.0, 1.0);
        float decoA = clamp(0.5 - d, 0.0, 1.0) * mix(rad.z, rad.w, t);
        color = mix(color, vec3(1.0), decoA / max(decoA + a * (1.0 - decoA), 1e-4));
        a = decoA + a * (1.0 - decoA);
    }

    a *= Info.w;
    if (a <= 0.002) {
        discard;
    }
    fragColor = vec4(color, a);
}
