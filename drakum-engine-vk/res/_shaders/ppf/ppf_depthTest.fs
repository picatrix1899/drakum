#version 400 core

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform sampler2D frameSrc;
uniform sampler2D frameDst;

uniform float near;
uniform float far;


float LinearizeDepth(float depth)
{
    float z = depth * 2.0 - 1.0; // Back to NDC 
    return (2.0 * near * far) / (far + near - z * (far - near));
}

void main()
{             
    float depthSrc = texture(frameSrc, pass_texCoords).r;
    float depthDst = texture(frameDst, pass_texCoords).r;
    
    float linSrc = LinearizeDepth(depthSrc) / far;
    float linDst = LinearizeDepth(depthDst) / far;
 
    if(linSrc > 1)
    {
    	out_Color = vec4(vec3(linDst), 1.0);
    	gl_FragDepth = depthDst;
    }
    else
    {
    	if(linSrc <= linDst)
    	{
			out_Color = vec4(vec3(linSrc), 1.0);
			gl_FragDepth = depthSrc;
    	}
    	else
    	{
    		out_Color = vec4(vec3(linDst), 1.0);
    		gl_FragDepth = depthDst;
    	}
    }
}  