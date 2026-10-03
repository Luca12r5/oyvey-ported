#version 330

uniform sampler2D InSampler;

layout(std140) uniform GradeConfig {
    vec4 Tint;
    float Saturation;
    float Contrast;
    float Brightness;
    float Sepia;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 c = texture(InSampler, texCoord).rgb;
    float l = dot(c, vec3(0.2126, 0.7152, 0.0722));
    c = mix(vec3(l), c, Saturation);
    c = (c - 0.5) * Contrast + 0.5;
    c *= Brightness;
    vec3 sep = vec3(dot(c, vec3(0.393, 0.769, 0.189)), dot(c, vec3(0.349, 0.686, 0.168)), dot(c, vec3(0.272, 0.534, 0.131)));
    c = mix(c, sep, Sepia);
    c *= Tint.rgb;
    fragColor = vec4(clamp(c, 0.0, 1.0), 1.0);
}
