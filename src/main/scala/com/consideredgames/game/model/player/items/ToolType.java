package com.consideredgames.game.model.player.items;

public enum ToolType {
	
        //Type         life    name              worth
    
	axe_stone_basic(	5,	"stone axe",			1)
	,axe_stone_fine(	10,	"fine stone axe",		2)
	,axe_iron_basic(	5,	"iron axe",				3)
	,axe_iron_fine(		10,	"fine iron axe",		4)

	// miningTools 
	,pickaxe_stone_basic(5,	"stone pick",			1)
	,helmet_wood(		6,	"wooden helmet",		2)
	,helmet_iron(		10,	"iron helmet",			3)
	//helmet_bone
	,pickaxe_stone_fine(10,	"fine stone pick",		2)
	,pickaxe_iron_basic(7,	"iron pick",			3)
	,pickaxe_iron_fine(	10,	"fine iron pick",		4)

	// farmingAgriculturetools 
	,scythe_bone(		5,	"bone scythe",			1)
	,scythe_stone_basic(5,	"stone scythe",			1)
	,scythe_stone_fine(	10,	"fine stone scythe", 	2)
	,scythe_iron(		10,	"iron scythe",			3)
	,plough_stone_basic(5,	"stone plough",			1)
	,plough_stone_fine(	10,	"fine stone plough", 	2)
	,plough_iron(		10,	"iron plough",			3)

	// farmingArabletools 
	,fence_wood_basic(	5,	"wooden fence",			1)
	,fence_wood_fine(	10,	"fine wooden fence", 	2)
	,fence_stone(		10,	"stone wall",			3)
	,trough_wood(		5,	"wooden trough",		4)
	,trough_stone(		10,	"stone trough",			4)
	,horse_bridle(		10,	"horse bridle",			4)
	,boar_brush(		10,	"boar brush",			4)
	,rabbit_trap(		10,	"rabbit trap",			4)

	// fighterTools 
	,spear(				5,	"spear",				1)
	,sword_stone(		5,	"stone sword",			2)
	,sword_iron_basic(	7,	"iron sword",			3)
	,sword_iron_fine(	10,	"fine iron sword",		4)

	// defenseTools
	//spear
	,shield_wood(		5,	"wooden shield",		1)
	,shield_iron_basic(	10,	"iron shield",			2)
	,shield_iron_fine(	12,	"fine iron shield",		3)
	,helmet_hide(		5,	"leather helmet",		1)
	,helmet_bone(		5,	"bone helmet",			1)
	,armour_hide(		7,	"leather armour",		2)
	,armour_iron_basic(	10,	"iron armour",			2)
	,armour_iron_fine(	12,	"fine iron armour",		3)

	// carpenterTools 
	,hammer_stone_basic(5,	"stone hammer",			1)
	,hammer_stone_fine(	10,	"fine stone hammer",    2)
	,hammer_iron(		10,	"iron hammer",			3)
	,mallet_wood(		5,	"wooden mallet",		1)
	,saw_iron(			10,	"iron saw",				2)
	,plane(				10,	"plane",				2) //?
	//axe_stone_basic
	//axe_stone_fine
	//axe_iron_ basic
	//axe_iron_fine

	// masonTools 
	,chisel_stone(		5,	"stone chisel",			1)
	,chisel_iron(		10,	"iron chisel",			2)
	,sledgehammer_stone(10,	"stone sledgehammer",   2)

	// fishingTools 
	//spear
	,rod_basic(			10,	"fishing rod",			1)
	,rod_fine(			10,	"fine fishing rod",		2)
	,net(				10,	"net",					1)
	,boat(				15,	"boat",					4)
	,boat_fine(			25,	"fine boat",			8)

	// ironmonger
	,furnace_stone(		10,	"stone furnace",		3)
	,furnace_clay(		10,	"clay furnace",			3)
	,smelter(			10,	"smelter",				4)
	
	,prongs(			10,	"prongs",				3)
	,anvil(				10,	"anvil",				3)
	,apron_hide(		10,	"leather apron",		2)


	// goldsmith
	//furnace_clay
	//furnace_stone
	//prongs
	//knife
	,knife_diamond(		15,	"diamond-edged knife",	5)

	// reproduction
	,cushion(			5,	"cushion",	        	1)
	,mat_hide(			5,	"leather mat",	        2)
	,bed(				10,	"bed",		        	4)
	,pillow_feather(	10,	"feather pillow",		3)

	// potter
	//clay_furnace
	//stone_furnace
	//prongs
	,turn_table(		10,	"turn table",			2)

	// clothier
	,needle_wood(		5,	"wooden needle",		1)
	,needle_iron(		10,	"iron needle",			3)
	,needle_bone(		5,	"bone needle",			1)
	,thimble(			10,	"thimble",				3)
	,knife_bone(		5,	"bone knife",			1)
	,knife_stone_basic(	5,	"stone knife",			1)
	,knife_stone_fine(	10,	"fine stone knife",		2)
	,knife_iron_basic(	10,	"iron knife",			3)
	,knife_iron_fine(	10,	"fine iron knife",		4)

	// cook
	,spoon_wood(		10,	"wooden spoon",			1)
	,pot_clay(			10,	"clay pot",				2)
	,pot_iron(			10,	"iron pot",				3) //(steel gives no benefit, just alternative but steel is more points)
	,oven_clay(			10,	"clay oven",			4)
	,oven_stone(		10,	"stone oven",			4); //  oven=cheaper furnace? Can downgrade/upgrade one into the other?

	// (cooking uses fuel)
	// 2/3 food slots at first.  

	//furnaces- different ones are better for different workshops? Stone, clay are equal?
	//smelter for iron

	//when slaughter animals (not fish) get bones as a by-product. More bones from bigger animals. Bones are better for making tools then stone and wood at times, there is not a bone workshop - either the carpenter or mason (or anyone?) can make tools with them. But not effected by skill or equipment? Only basic stuff. Bowl, needle, knife, 

	//bow and arrow?
	
	/** Tools degrade with use */
	private int startLife;
	/** Creating tools earns points */
	private int worth;
	private String name;
	
	private ToolType(int life, String name, int worth){
		this.startLife = life;
		this.worth = worth;
		this.name = name;
	}
	
	public int getStartLife(){
		return startLife;
	}
	
	public int getWorth(){
		return worth;
	}
	
	public String toString(){
		return name;
	}
	
}
