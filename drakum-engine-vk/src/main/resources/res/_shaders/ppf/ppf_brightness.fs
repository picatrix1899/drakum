#version 400 core

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform sampler2D textureMap;

void main()
{
	vec4 color = texture(textureMap, pass_texCoords);

	//float brightness = 1.0f;
	float brightness = dot(color.rgb, vec3(0.2126,0.7152, 0.0722));
	
	if(brightness > 1.0f)
	{
		out_Color = color;
	}
	else
	{
		out_Color = vec4(0.0,0.0,0.0,1.0);
	}

	float depth = color.r;

	float z = depth * 2.0 - 1.0;

	float near = 0.1;
	float far = 1000;

	float r = (2.0 * near * far) / (far + near - z * (far -near));

	r /= far;

	out_Color = vec4(vec3(r), 1.0f);
	
	//float brightness = (color.r * 0.2126) + (color.g * 0.7152) + (color.b * 0.0722);
	//out_Color = color * brightness;
}