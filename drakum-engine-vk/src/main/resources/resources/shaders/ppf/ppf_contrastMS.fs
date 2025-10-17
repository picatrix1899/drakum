#version 400 core

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform sampler2DMS textureMap;

uniform float contrast;

void main()
{
	
	out_Color = texelFetch(textureMap, ivec2(gl_FragCoord.xy), gl_SampleID);
	out_Color.rgb = (out_Color.rgb -0.5) * (1.0 + contrast) + 0.5;
}