#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec2 Local;
in vec4 Shape;
in vec4 Extra;

out vec4 vColor;
out vec2 vUv;
out vec2 vLocal;
flat out vec4 vShape;
flat out vec4 vExtra;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vColor = Color * ColorModulator;
    vUv = UV0;
    vLocal = Local;
    vShape = Shape;
    vExtra = Extra;
}
