#version 330

// Photo textured character: a soft key light from above the front, lit like the world around him.
// While the headset's pointer is on him he gets a rim of light, like a selected object.

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 vUv;
in vec3 vNormal;
in vec4 vLight;
in vec3 vViewPosition;
in vec3 vViewNormal;
in vec4 vGlow;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, vUv);
    if (color.a < 0.05) {
        discard;
    }
    vec3 n = vNormal / max(length(vNormal), 1e-5);
    float shade = 0.72 + 0.28 * max(0.0, dot(n, normalize(vec3(-0.35, 0.8, 0.45))));
    vec3 rgb = color.rgb * shade * vLight.rgb;
    if (vGlow.a > 0.001) {
        // Both sides of the mesh are drawn, so the normal may face either way.
        vec3 viewNormal = vViewNormal / max(length(vViewNormal), 1e-5);
        float facing = abs(dot(viewNormal, normalize(-vViewPosition)));
        float rim = pow(1.0 - facing, 1.8);
        vec3 glow = mix(vGlow.rgb, vec3(1.0), 0.25);
        rgb = mix(rgb, glow, clamp(rim * 1.25 + 0.08, 0.0, 0.92) * vGlow.a);
    }
    fragColor = vec4(rgb, 1.0) * ColorModulator;
}
