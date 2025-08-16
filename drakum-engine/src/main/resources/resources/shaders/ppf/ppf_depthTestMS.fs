#version 400 core

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform sampler2DMS frameSrc;
uniform sampler2DMS frameDst;
uniform sampler2DMS depthSrc;
uniform sampler2DMS depthDst;

uniform float near;
uniform float far;

void main()
{             
    float dSrc = texelFetch(depthSrc, ivec2(gl_FragCoord.xy), 0).r;
    float dDst = texelFetch(depthDst, ivec2(gl_FragCoord.xy), 0).r;
 
    if(dSrc > 1)
    {
    	out_Color = texelFetch(frameDst, ivec2(gl_FragCoord.xy), 0);
    	gl_FragDepth = dDst;
    }
    else
    {
    	if(dSrc < dDst)
    	{
			out_Color = texelFetch(frameSrc, ivec2(gl_FragCoord.xy), 0);
			gl_FragDepth = dSrc;
    	}
    	else
    	{
    		out_Color = texelFetch(frameDst, ivec2(gl_FragCoord.xy), 0);
    		gl_FragDepth = dDst;
    	}
    }
    
    
    //out_Color = texelFetch(frameSrc, ivec2(gl_FragCoord.xy), gl_SampleID);
}  