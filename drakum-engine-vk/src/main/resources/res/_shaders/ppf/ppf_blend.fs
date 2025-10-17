#version 400 core

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform sampler2D scene1;
uniform sampler2D scene2;

uniform int src;
uniform int dst;
uniform int op;

const int OP_ADD = 0;
const int OP_SUB = 1;
const int OP_MUL = 2;
const int OP_DIV = 3;

const int ZERO = 0;
const int ONE = 1;
const int SRC_COLOR = 2;
const int ONE_MINUS_SRC_COLOR = 3;
const int DST_COLOR = 4;
const int ONE_MINUS_DST_COLOR = 5;
const int SRC_ALPHA = 6;
const int ONE_MINUS_SRC_ALPHA = 7;
const int DST_ALPHA = 8;
const int ONE_MINUS_DST_ALPHA = 9;

vec4 getComponents(int i, vec4 inv, vec4 s, vec4 d)
{
	vec4 o;

	if(i == ZERO)
	{
		o = inv * 0.0f;
	}
	else if(i == ONE)
	{
		o = inv * 1.0f;
	}
	else if(i == SRC_COLOR)
	{
		o = inv * s;
	}
	else if(i == ONE_MINUS_SRC_COLOR)
	{
		o = inv * (1 - s);
	}
	else if(i == DST_COLOR)
	{
		o = inv * d;
	}
	else if(i == ONE_MINUS_DST_COLOR)
	{
		o = inv * (1 - d);
	}
	else if(i == SRC_ALPHA)
	{
		o = inv * s.a;
	}
	else if(i == ONE_MINUS_SRC_ALPHA)
	{
		o = inv * (1 - s.a);
	}
	else if(i == DST_ALPHA)
	{
		o = inv * d.a;
	}
	else if(i == ONE_MINUS_DST_ALPHA)
	{
		o = inv * (1 - d.a);
	}
	else
	{
		o = vec4(0.0f);
	}
	
	return o;
}

void main()
{
	vec4 s = texture(scene1, pass_texCoords);
	vec4 d = texture(scene2, pass_texCoords);
	
	
	vec4 s1 = getComponents(src, s, s, d);
	vec4 d1 = getComponents(dst, d, s, d);

	vec4 o;
	
	if(op == OP_ADD)
	{
		o = s1 + d1;
	}
	else if(op == OP_SUB)
	{
		o = s1 - d1;
	}
	else if(op == OP_MUL)
	{
		o = s1 * d1;
	}
	else if(op == OP_DIV)
	{
		o = s1 / d1;
	}
	else
	{
		o = vec4(0.0f);
	}
	
	out_Color = o * vec4(1,1,1,1);
}