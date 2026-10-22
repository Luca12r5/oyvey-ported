#version 330

// Simple physically-inspired material for the headset: vanilla two-light diffuse (so it matches
// the player's arms), Blinn-Phong highlights, a fresnel rim and a soft sky/ground reflection.
// vMaterial.x = specular strength, vMaterial.y = glossiness.

#moj_import <minecraft:light.glsl>
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in vec4 vColor;
in vec2 vMaterial;
in vec3 vNormal;
in vec3 vPosition;
in vec4 vLight;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;

out vec4 fragColor;

void main() {
    vec3 n = normalize(vNormal);
    vec3 v = normalize(-vPosition);
    if (dot(n, v) < 0.0) {
        n = -n;
    }
    float specular = vMaterial.x;
    float gloss = vMaterial.y;

    // Diffuse: Minecraft's entity lighting, slightly softened.
    vec2 light = vec2(max(dot(Light0_Direction, n), 0.0), max(dot(Light1_Direction, n), 0.0));
    float diffuse = min(1.0, (light.x + light.y) * 0.55 + 0.45);
    vec3 color = vColor.rgb * diffuse;

    // Highlights from both scene lights.
    float shininess = mix(10.0, 220.0, gloss * gloss);
    float normalization = (shininess + 8.0) / 60.0;
    vec3 h0 = normalize(Light0_Direction + v);
    vec3 h1 = normalize(Light1_Direction + v);
    float highlight = pow(max(dot(n, h0), 0.0), shininess) + 0.6 * pow(max(dot(n, h1), 0.0), shininess);
    color += vec3(highlight * normalization * specular * 0.55);

    // Reflection of a soft environment: bright above, dark below.
    vec3 r = reflect(-v, n);
    float sky = smoothstep(-0.25, 0.85, r.y);
    vec3 environment = mix(vec3(0.06, 0.06, 0.07), vec3(0.78, 0.82, 0.88), sky);
    float fresnel = pow(1.0 - max(dot(n, v), 0.0), 4.0);
    float reflectivity = specular * mix(0.04, 0.22, gloss) + fresnel * specular * 0.35;
    color = mix(color, environment, clamp(reflectivity, 0.0, 0.6));

    // World light level (sky + block light) like every other entity.
    color *= vLight.rgb;
    fragColor = apply_fog(vec4(color, vColor.a), sphericalVertexDistance, cylindricalVertexDistance,
        FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
