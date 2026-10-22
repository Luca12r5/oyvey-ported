#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;

out vec4 vColor;
out vec2 vMaterial;
out vec3 vNormal;
out vec3 vPosition;
out vec4 vLight;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // Position is relative to the camera with world orientation; the camera sits at the origin.
    vPosition = Position;
    vNormal = Normal;
    vColor = Color;
    vMaterial = UV0;
    vLight = texelFetch(Sampler2, UV2 / 16, 0);
    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
}
