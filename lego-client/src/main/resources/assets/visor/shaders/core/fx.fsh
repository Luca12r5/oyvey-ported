#version 330

// Headset screen effects, all composited as black over the frame:
//  - the soft rounded field-of-view edge of the lenses while the headset is on,
//  - the two lens openings that close (or open) while the headset passes the eyes,
//  - a plain blackout used during boot.

layout(std140) uniform FxData {
    vec4 Screen;  // width, height, time, unused
    vec4 Lens;    // x = fov vignette strength, y = blackout, z = lens closure (0 open .. 1 closed), w = lens mask strength
    vec4 Glow;    // rgb = boot glow colour, a = glow strength
    vec4 Shape;   // x = merge (0 = two lenses, 1 = one soft "iris" opening in the centre)
};

out vec4 fragColor;

float sdRoundRect(vec2 p, vec2 halfSize, float r) {
    vec2 q = abs(p) - halfSize + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

void main() {
    vec2 res = Screen.xy;
    vec2 frag = vec2(gl_FragCoord.x, res.y - gl_FragCoord.y);
    // Coordinates where the screen height spans -1..1.
    vec2 p = (frag - 0.5 * res) / (0.5 * res.y);
    float aspect = res.x / res.y;

    // 1. Lens field-of-view edge: dark, very soft rounded rectangle border.
    float edge = sdRoundRect(p, vec2(aspect * 0.98, 0.98), 0.55);
    float vignette = smoothstep(-0.55, 0.12, edge) * Lens.x;
    vignette += smoothstep(0.35, 1.6, length(p * vec2(0.82, 1.0))) * Lens.x * 0.35;

    // 2. Lens openings closing towards black: two lenses while the headset approaches the eyes,
    // or one wide soft opening (like eyes adjusting) when the view comes back.
    float closure = clamp(Lens.z, 0.0, 1.0);
    float merge = clamp(Shape.x, 0.0, 1.0);
    float radius = mix(1.25 * aspect, 0.0, smoothstep(0.0, 1.0, closure));
    float spread = mix(0.62, 0.42, closure) * (1.0 - merge);
    vec2 squash = mix(vec2(1.0, 0.92), vec2(0.62, 1.0), merge);
    vec2 left = p - vec2(-spread, 0.0);
    vec2 right = p - vec2(spread, 0.0);
    float lens = min(length(left * squash), length(right * squash)) - radius;
    float softness = mix(0.35, 0.08, closure) * mix(1.0, 1.6, merge);
    float lensMask = smoothstep(-softness, softness * 0.4, lens) * Lens.w;

    float alpha = clamp(max(max(vignette, lensMask), Lens.y), 0.0, 1.0);

    // Faint coloured bloom used while booting (behind the logo); only visible on the black.
    vec3 color = Glow.rgb * (Glow.a * exp(-dot(p, p) * 2.2));

    if (alpha <= 0.001) {
        discard;
    }
    fragColor = vec4(color, alpha);
}
