package dev.lego.emote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class EmotePack {
   private static final Map<String, float[]> P = new HashMap<>();
   private static final String[] PARTS = new String[]{"H", "B", "RA", "LA", "RL", "LL"};
   private static final String[] DATA = new String[]{
      "d_disco2|Disco-Fieber|music|6|L|0|-|2.2|DISC1+L0>DISC2+LEGW|sway 0.08 2.2",
      "d_ymca|YMCA|music|6|L|0|-|2.4|YMC_Y>YMC_M>YMC_C>YMC_A|bob 0.05 2.4",
      "d_egypt|Wie ein Ägypter|music|6|L|0|-|2|EGY1>EGY2|twist 0.15 1",
      "d_sprinkler|Rasensprenger|music|6|L|0|-|5|SPRK>SPRK2|shake 0.2 2.5",
      "d_raise|Dach hochheben|music|6|L|0|-|3|RAISE1>RAISE2|bob 0.06 3",
      "d_wave|Armwelle|music|6|L|0|-|2.5|WAVEL>T>WAVER>T|tilt 0.12 1.25",
      "d_roll|Rollen-Tanz|music|6|L|0|-|5|ROLL1>ROLL2|sway 0.1 1.5",
      "d_hula2|Hula-Hula|music|6|L|0|-|2|HULA1>HULA2|sway 0.12 1",
      "d_charleston|Charleston|music|6|L|0|-|3|KICKR+CLAP2>BACKKL+CLAP1>KICKL+CLAP2>BACKKR+CLAP1|-",
      "d_twist|Twist|music|6|L|0|-|3|TWL+SNAP+SQUAT>TWR+SNAP+SQUAT|-",
      "d_cancan|Cancan|music|6|L|0|-|3|KICKR+HIPS>A0+HIPS+L0>KICKL+HIPS>A0+HIPS+L0|-",
      "d_irish|Irischer Stepptanz|music|6|L|0|-|5|TOER+A0>L0+A0>TOEL+A0>L0+A0|bob 0.05 5",
      "d_stomp2|Stampf-Tanz|music|6|L|0|-|3|KNEE+FISTS>L0+FISTS|-",
      "d_jumpjack|Party-Hampelmann|music|6|L|0|-|3|STAR>STAR0|nod 0.1 3",
      "d_shake|Schüttel-Tanz|music|6|L|0|-|2|V>FWD2|sway 0.15 6",
      "d_handjive|Hand-Jive|music|6|L|0|-|4|CLAP1>CLAP2>FWD2>CLAP1|bob 0.05 4",
      "d_clubfist|Club-Faust|music|6|L|0|-|4|UPR>FWDR|nod 0.15 4",
      "d_cowboy|Cowboy-Tanz|music|6|L|0|-|2.5|HIPS+TOER>HIPS+TOEL|bob 0.05 2.5",
      "d_skank|Ska-Tanz|music|6|L|0|-|4|RUN1>RUN2|bob 0.08 4",
      "d_vogue|Vogue|music|6|L|0|-|1.6|HEAD2>FACE2>V>HIPS|tilt 0.15 0.8",
      "d_pogo|Pogo|music|6|L|0|-|4|A0+L0>JUMPP|-",
      "d_conga|Conga|music|6|L|0|-|2|FWD2+KICKR>FWD2+L0>FWD2+KICKL>FWD2+L0|sway 0.1 1",
      "d_salsa|Salsa|music|6|L|0|-|3|HULA1+TOER>HULA2+TOEL|twist 0.12 1.5",
      "d_tango|Tango|music|6|L|0|-|1.5|T+LUNGE+TWL>T+L0>T+LUNGEL+TWR>T+L0|-",
      "d_waltz|Walzer|music|6|L|90|-|1.5|GUARD+TOER>GUARD+TOEL|sway 0.1 0.75",
      "d_breakdance|Breakdance-Arme|music|6|L|0|-|4|UP2>SQUAT+FWD2>T>SQUAT+CROSS|-",
      "d_hiphop|Hip-Hop-Wippen|music|6|L|0|-|2.5|CROSS>HIPS|nod 0.18 2.5",
      "d_popping|Popping|music|6|L|0|-|6|T>WAVEL>T>WAVER|-",
      "d_locking|Locking|music|6|L|0|-|3|POINTF>POINTU>FWD2>A0|bob 0.06 3",
      "d_tutting|Tutting|music|6|L|0|-|2|FWD2>CROSS>T>BACK2|-",
      "d_lean|Lean Back|music|6|L|0|-|1|LEANB+HIPS>H0+B0+HIPS|-",
      "d_sway|Hin und her wiegen|music|6|L|0|-|1|LEANL+A0>LEANR+A0|-",
      "d_handsup|Hände hoch Party|music|6|L|0|-|2|UP2>V|bob 0.08 2",
      "d_bounce|Bounce|music|6|L|0|-|3|A0+L0>A0+SQUAT|nod 0.12 3",
      "d_step|Seitschritt|music|6|L|0|-|2|SIDEKR+A0>L0+A0>SIDEKL+A0>L0+A0|-",
      "d_robot2|Roboter 2.0|chip|6|L|0|-|2|FWDR>FWD2>T>A0|twist 0.3 0.5",
      "d_crank|Kurbel-Tanz|music|6|L|0|-|4|ROLL1+SQUAT>ROLL2+L0|-",
      "d_swim2|Schwimm-Tanz|music|6|L|0|-|3|SWIM1>SWIM2|sway 0.1 1.5",
      "d_lasso2|Lasso-Tanz|music|6|L|0|-|2|UPR+HIPS>UPR+L0|wave 0.6 3",
      "d_fever|Saturday Night|music|6|L|0|-|2|DISC1+LEANL>DISC2+LEANR|-",
      "d_snake|Schlangentanz|music|6|L|0|-|1.5|SNAKE>SNAKE+LEANL>SNAKE>SNAKE+LEANR|-",
      "d_tribal|Stammestanz|music|6|L|0|-|3|WINGU+KNEE>WINGD+L0|-",
      "d_bollywood|Bollywood|music|6|L|0|-|2.5|UPR+FWDL+TOER>UPL+FWDR+TOEL|tilt 0.15 1.25",
      "d_kpop|K-Pop-Choreo|music|6|L|0|-|3|POINTF>CROSS>V>HIPS|bob 0.06 3",
      "d_line|Line Dance|music|6|L|0|-|2|HIPS+TOER>HIPS+KICKR>HIPS+TOEL>HIPS+KICKL|-",
      "d_jive|Jive|music|6|L|0|-|4|SNAP+TOER>SNAP+TOEL|twist 0.1 2",
      "d_reggae|Reggae-Groove|music|6|L|0|-|1.5|A0+LEANF>A0+B0|nod 0.12 1.5",
      "d_wobble|Wobble|music|6|L|0|-|2|SQUAT+FWD2>L0+FWD2|sway 0.15 2",
      "d_slide|Electric Slide|music|6|L|0|-|1.5|SIDEKR+SNAP>L0+SNAP>SIDEKL+SNAP>L0+SNAP|-",
      "d_hammer|Hammer-Time|music|6|L|0|-|4|RUN1+SQUAT>RUN2+SQUAT|shake 0.2 2",
      "d_gangnam|Pferde-Galopp|music|6|L|0|-|4|STEER+TOER>STEER+TOEL|bob 0.06 4",
      "d_carlton|Carlton|music|6|L|0|-|3|WINGD+TWL>WINGD+TWR>UPR>A0|-",
      "d_whip|Whip|music|6|L|0|-|2|FWDR+SQUAT>SWU+L0|-",
      "d_nae|Nae Nae|music|6|L|0|-|1.5|UPR+LEANL>UPR+LEANR|wave 0.3 3",
      "d_swing|Swing|music|6|L|0|-|3|RUN1+SNAP>RUN2+SNAP|-",
      "d_mashed|Mashed Potato|music|6|L|0|-|4|PAWS+TOER>PAWS+TOEL|twist 0.12 2",
      "d_bird|Vogeltanz|music|6|L|0|-|4|WINGU>WINGD|nod 0.12 4",
      "d_jumpstyle|Jumpstyle|music|6|L|0|-|4|KICKR+A0>BACKKR+A0>KICKL+A0>BACKKL+A0|-",
      "d_shuffle2|Cutting Shapes|music|6|L|0|-|4|SIDEKR+T>L0+CROSS>SIDEKL+T>L0+CROSS|-",
      "d_belly|Bauchtanz|music|6|L|0|-|1|HEAD2+HULA1>HEAD2+HULA2|sway 0.15 2",
      "d_flamenco|Flamenco|music|6|L|0|-|2|CLAP2+UPR>HCLAP1>CLAP2+UPL>HCLAP1|bob 0.05 4",
      "d_polka|Polka|music|6|L|0|-|3|HIPS+KICKR>HIPS+KICKL|sway 0.1 1.5",
      "d_samba|Samba|music|6|L|0|-|4|HULA1+TOER>HULA2+TOEL|bob 0.06 4",
      "d_rave|Rave|music|6|L|0|-|5|POINTU>FWDR>POINTU+UPL>A0|-",
      "d_moonhop|Mond-Hüpfer|music|6|L|0|-|2|JUMPP>A0+L0|-",
      "d_zigzag|Zickzack|music|6|L|0|-|3|LEANL+WAVEL>LEANR+WAVER|-",
      "d_slowmo|Zeitlupen-Tanz|music|8|L|0|-|0.6|V>T>FWD2>A0|-",
      "d_spinner|Kreisel|refresh|5|L|540|-|1|T|-",
      "d_sidejump|Seitensprünge|music|6|L|0|-|3|SIDEKR+V>STAR0>SIDEKL+V>STAR0|-",
      "d_sixseven|67 Remix|star|6|L|0|-|4|FWD2+TOER>CLAP2+TOEL|nod 0.1 4",
      "s_basket|Korbwurf|target|2.4|O|0|-|2|BALL1>BALL2>A0|-",
      "s_dribble|Dribbeln|target|6|L|0|-|4|POINTD>FWDR+SQUAT|-",
      "s_golf|Golfschwung|flag|3|O|0|-|1.5|GOLF1>GOLF2>A0|-",
      "s_tennis|Vorhand|target|2.5|O|0|-|2|TEN1>TEN2>A0|-",
      "s_baseball|Baseball-Schlag|hit|2.5|O|0|-|2|BAT1>BAT2>A0|-",
      "s_goalie|Torwart|shield|6|L|0|-|2|GOALIE+LEANL>GOALIE+LEANR|-",
      "s_dive|Hechtsprung|jump|2.6|O|0|-|2|A0>DIVE>DIVE|-",
      "s_skate|Eiskunstlauf|snow|6|L|0|-|1.5|SKATE1>SKATE2|-",
      "s_lift|Gewichtheben|trophy|4|O|0|-|1|LIFT1>LIFT1>LIFT2>LIFT2|-",
      "s_javelin|Speerwurf|arrow|2.6|O|0|-|2|JAV>THR2>A0|-",
      "s_sprintstart|Sprintstart|sprint|3|O|0|-|1.5|SPRINT0>SPRINT0>RUN1|-",
      "s_jog|Joggen|run|6|L|0|-|3|RUN1>RUN2|-",
      "s_pushups|Kniebeugen|sprint|6|L|0|-|1.2|FWD2+L0>FWD2+SQUAT|-",
      "s_jumpingjack|Sport-Hampelmann|jump|6|L|0|-|2.5|STAR>STAR0|-",
      "s_boxing2|Schattenboxen|hit|6|L|0|-|4|PUNR>GUARD>PUNL>GUARD|-",
      "s_uppercut|Aufwärtshaken|hit|2.2|O|0|-|3|GUARD>POW>BOXUP>GUARD|-",
      "s_kickbox|Kickboxen|hit|6|L|0|-|2.5|GUARD+KICKR>GUARD+L0>GUARD+SIDEKL>GUARD+L0|-",
      "s_fencing|Fechten|sword|6|L|0|-|2|SWF+LUNGE>GUARD+L0|-",
      "s_archery|Bogenschießen (Sport)|target|3|O|0|-|1.2|BOWD>BOWD>BOWR|-",
      "s_row|Kajak|flag|6|L|0|-|1.5|PULL1+TWL>PULL2+TWR|-",
      "s_climb2|Bouldern|jump|6|L|0|-|2|UPR+KICKL>UPL+KICKR|-",
      "s_cycle2|Bergauf radeln|speed|6|L|0|-|3|STEER+KICKR+LEANF>STEER+KICKL+LEANF|-",
      "s_ski2|Slalom|snow|6|L|0|-|1.5|BACK2+SQUAT+LEANL>BACK2+SQUAT+LEANR|-",
      "s_surf2|Wellenreiten|rain|6|L|0|-|0.8|T+LUNGE+LEANL>T+LUNGE+LEANR|-",
      "s_yoga2|Krieger-Pose|leaf|5|O|0|zen|1|A0>T+LUNGE+LR|-",
      "s_yoga3|Sonnengruß|sun|6|O|0|zen|0.8|A0>UP2+LEANB>BENDF>A0|-",
      "s_stretch2|Seitlich dehnen|leaf|5|O|0|-|0.8|UP2>UPR+LEANL>UPL+LEANR>A0|-",
      "s_toe|Zehen berühren|leaf|4|O|0|-|0.8|A0>BENDF>BENDF>A0|-",
      "s_referee|Schiedsrichter|flag|3|O|0|-|2|POINTSR>POINTSR>A0|shake 0.1 2",
      "s_redcard|Rote Karte|flag|2.6|O|0|-|2|A0>POINTU>POINTU|-",
      "s_goal|Tor!|trophy|4|L|0|-|3|RAISE1+RUN1>RAISE1+RUN2|-",
      "s_knee|Knierutscher|trophy|3|O|0|-|1.5|A0>KNEE+V>KNEE+V|-",
      "s_medal|Medaille zeigen|trophy|3|O|0|-|1|A0>MOUTHR>PRESENT|-",
      "s_podium|Siegerpodest|trophy|4|O|0|-|1|A0>UP2>V+LUP|-",
      "s_bowling|Bowling|target|2.8|O|0|-|1.5|BACK2>LUNGE+FWDR>LUNGE+POINTF|-",
      "s_dart|Darts|target|2.4|O|0|-|2|FWDR>MOUTHR>POINTF|-",
      "s_volley|Baggern|target|2.4|O|0|-|2|FWD2+SQUAT>FWD2>A0|-",
      "s_handstand|Handstand-Versuch|jump|3|O|0|-|1.5|UP2>BENDF+UP2>A0|-",
      "s_cartwheel|Rad schlagen|refresh|2.4|O|360|-|2|STAR>STAR>STAR0|-",
      "s_trampoline|Trampolin|jump|6|L|0|-|2.5|JUMPP>A0+L0|-",
      "j_chef|Koch|food|5|L|0|-|2|STIR>CHEF|-",
      "j_waiter|Kellner|food|4|L|0|-|1|WAITER+TOER>WAITER+TOEL|-",
      "j_police|Polizist|shield|4|O|0|-|1.5|A0>POLICE>COP2|-",
      "j_traffic|Verkehr regeln|shield|6|L|0|-|1.5|POINTSR>STOP2>POINTSL>STOP2|-",
      "j_pilot|Pilot|wing|6|L|0|-|1|PILOT>T+LEANR|-",
      "j_scientist|Wissenschaftler|flask|5|L|0|-|1.5|SCIENT>SCIENT+TL|-",
      "j_doctor|Arzt|plus|5|L|0|-|1.2|DOC>DOC+TR|-",
      "j_builder|Bauarbeiter|brick|6|L|0|-|2.5|HAM1>HAM2|-",
      "j_teacher|Lehrer|info|5|L|0|-|1|TEACH>TEACH+TR|-",
      "j_farmer|Bauer|leaf|6|L|0|-|1.5|DIG1>DIG2|-",
      "j_astronaut|Astronaut|moon|8|L|0|-|0.5|ASTRO>ASTRO+TL|bob 0.05 0.5",
      "j_pirate|Pirat|flag|4|O|0|-|1.2|A0>PIRATE>PIRATE+LUP|-",
      "j_spy|Geheimagent|eye|6|L|0|-|1.5|SNEAK1>SNEAK2|-",
      "j_painter|Maler|palette|6|L|0|-|2|PAINT1>PAINT2|-",
      "j_carpenter|Schreiner|box|6|L|0|-|3|SAW1>SAW2|-",
      "j_sweep|Fegen|trash|6|L|0|-|1.5|SWEEP1>SWEEP2|-",
      "j_dishes|Abwaschen|rain|6|L|0|-|3|WASH>WASH+TL|-",
      "j_carry|Kiste tragen|box|6|L|0|-|2|CARRY+WALK1>CARRY+WALK2|-",
      "j_type|Tippen|keyboard|6|L|0|-|6|TYPE>TYPE+ROLL1|-",
      "j_read|Buch lesen|bookmark|6|L|0|-|0.3|BOOK>BOOK+TL|-",
      "j_text|Am Handy|chat|6|L|0|-|0.5|TEXT>TEXT+TR|-",
      "j_call2|Anrufen|bell|5|L|0|-|0.6|PHONE>PHONE+LL>PHONE>PHONE+LR|-",
      "j_watch|Auf die Uhr schauen|clock|3|O|0|-|1|A0>WATCH>WATCH|-",
      "j_photo|Foto machen|camera|3|O|0|-|1.5|A0>PICT>PICT|-",
      "j_selfie2|Gruppen-Selfie|camera|4|O|0|-|1|A0>SELF>SELF+TR|-",
      "j_conductor|Orchester leiten|music|6|L|0|-|2|CONDUCT>CONDUCT+UPR|wave2 0.3 2",
      "j_cheers|Prost|food|3|O|0|-|1.5|A0>CHEERS>MOUTHR|-",
      "j_eatcake|Kuchen essen|food|5|L|0|-|1.5|CAKE>EAT|-",
      "j_balloon|Ballon halten|gift|5|L|0|-|0.6|BALLOON>BALLOON+TL|-",
      "j_present|Geschenk überreichen|gift|3|O|0|-|1|A0>PRESENT>PRESENT|-",
      "j_umbrella|Regenschirm|rain|6|L|0|-|1|UMBR+WALK1>UMBR+WALK2|-",
      "j_kite|Drachen steigen lassen|wing|6|L|0|-|0.8|KITE>KITE+LEANL>KITE>KITE+LEANR|-",
      "j_mirror|In den Spiegel schauen|eye|4|O|0|-|1|A0>MIRROR>MIRROR+TL|-",
      "j_hair|Haare richten|sparkle|3|O|0|-|1.5|A0>HAIR>HAIR+TL|-",
      "j_money|Geld zählen|gem|5|L|0|-|4|MONEY>MONEY+TL|-",
      "j_coffee|Kaffee schlürfen|food|5|L|0|-|0.8|MOUTHR>FWDR|-",
      "j_garden|Gießen|leaf|6|L|0|-|1|FWDR+LEANF>FWDR+LEANF+TL|-",
      "j_hammer2|Nagel einschlagen|hit|4|L|0|-|3|HAM1+SQUAT>HAM2+SQUAT|-",
      "j_mop|Wischen|trash|6|L|0|-|2|SWEEP1+LEANF>SWEEP2+LEANF|-",
      "j_knock|Anklopfen|home|3|O|0|-|3|A0>FWDR>COME2>FWDR>COME2|-",
      "j_cashier|Kassierer|gem|5|L|0|-|2|TRADE>TRADE+LDN|-",
      "j_mail|Brief einwerfen|chat|3|O|0|-|1.5|A0>POINTF>POINTF|-",
      "j_magician|Zauberkünstler|wand|4|O|0|-|1.5|WAND>WANDS>TADA|-",
      "j_juggler|Clown|sparkle|6|L|0|laugh|3|DRUM1>DRUM2|nod 0.1 3",
      "j_guard|Wache stehen|shield|8|L|0|-|0.3|A0+H0>A0+LUP|-",
      "a_penguin2|Pinguin-Rutschen|paw|6|L|0|-|3|PENG+TOER>PENG+TOEL|sway 0.15 1.5",
      "a_elephant|Elefant|paw|6|L|0|-|1|TRUNK>TRUNK+LUP|-",
      "a_bunny2|Häschen|paw|6|L|0|-|3|EARS+L0>EARS+JUMPP|-",
      "a_cat|Katze|paw|6|L|0|-|1|PAWS>PAWS+TL|-",
      "a_cat2|Katzenpfote|paw|5|L|0|-|3|PAWS>COME2|-",
      "a_dog|Hund|paw|6|L|0|-|4|PAWS+SQUAT>PAWS+SQUAT+TL|-",
      "a_bear|Bär|paw|6|L|0|-|1.2|BEAR+TOER>BEAR+TOEL|-",
      "a_lion|Löwe brüllen|paw|3|O|0|-|2|A0>CLAW>CLAW+LUP|-",
      "a_owl|Eule|eye|6|L|0|-|0.8|OWL+LL>OWL+LR|-",
      "a_deer|Hirsch|leaf|5|O|0|-|1|A0>DEER>DEER+LL|-",
      "a_frog|Frosch|jump|6|L|0|-|2|FROG>JUMPP|-",
      "a_fish|Fisch|rain|6|L|0|-|2|FISHB+TL>FISHB+TR|sway 0.15 2",
      "a_snake2|Kobra|paw|6|L|0|-|1|SNAKE+LEANL>SNAKE+LEANR|-",
      "a_scorpion|Skorpion|paw|6|L|0|-|2|SCORP>SCORP+TL|-",
      "a_eagle|Adler|wing|6|L|0|-|1.2|WINGU+LEANF>WINGD+LEANF|-",
      "a_bat|Fledermaus|wing|6|L|0|-|4|VAMP>WINGU|-",
      "a_butterfly|Schmetterling|wing|6|L|0|-|5|WINGU+TOER>WINGD+TOER|-",
      "a_duck|Ente|paw|6|L|0|-|3|PENG+WALK1>PENG+WALK2|nod 0.15 3",
      "a_horse2|Pferd|paw|6|L|0|-|4|STEER+KICKR>STEER+KICKL|nod 0.1 4",
      "a_kangaroo|Känguru|jump|6|L|0|-|2.5|PAWS+L0>PAWS+JUMPP|-",
      "a_seal|Seehund|paw|6|L|0|-|3|PENG+LUP>CLAP1+LUP|-",
      "a_gorilla|Gorilla|paw|6|L|0|-|4|FISTS+PUNR>FISTS+PUNL|-",
      "a_flamingo|Flamingo|paw|6|L|0|-|0.5|WINGD+KICKL>WINGD+KICKL+TL|-",
      "a_crab2|Krebs-Zwicken|paw|6|L|0|-|3|CRAB>CRAB+CLAP2|-",
      "a_octopus|Krake|paw|6|L|0|-|3|WINGU+LEGW>WINGD+L0|wave2 0.4 3",
      "a_giraffe|Giraffe|paw|6|L|0|-|0.6|POINTU+LUP>POINTU+LUP+TL|-",
      "a_turtle|Schildkröte|paw|8|L|0|-|0.6|LEANF+PAWS+WALK1>LEANF+PAWS+WALK2|-",
      "a_shark|Hai|warning|6|L|0|-|2|PRAY+UP2>PRAY+FWD2|-",
      "a_spider|Spinne|paw|6|L|0|-|4|CLAW+SQUAT>WINGD+SQUAT|-",
      "a_rooster|Hahn|bell|3|O|0|-|1.5|WINGD>WINGU+LUP>WINGU+LUP|-",
      "f_sob|Schluchzen|rain|5|L|0|cry|2|CRY2>CRY2+TL|nod 0.1 4",
      "f_scared2|Panik|alert|5|L|0|-|5|SCARE+LL>SCARE+LR|-",
      "f_rage|Wutanfall|flame|5|L|0|-|5|FISTS+KNEE>FISTS+L0|-",
      "f_shy2|Verlegen|heart|5|L|0|-|0.8|SHY>SHY+TR|-",
      "f_proud|Stolz|crown|4|O|0|-|1|A0>PROUD>PROUD|-",
      "f_happy|Glücklich|sun|5|L|0|laugh|2|HAPPY>HAPPY+TL|-",
      "f_sad3|Niedergeschlagen|cloud|6|L|0|-|0.4|SAD2>SAD2+TL|-",
      "f_dizzy|Schwindelig|refresh|5|L|120|-|1|DIZZY>DIZZY+LEANL|-",
      "f_faint|Ohnmacht|skull|4|O|0|-|1|A0>FAINT>FAINT|-",
      "f_hiccup|Schluckauf|alert|5|L|0|-|1.5|A0>HICCUP+A0|-",
      "f_wait|Warten|hourglass|8|L|0|-|0.5|WAIT>WAIT+TL|-",
      "f_tada|Tadaa!|star|3|O|0|-|2|A0>TADA>TADA|-",
      "f_what|Was?!|info|3|O|0|-|2|A0>WHAT>WHAT+TL|-",
      "f_no|Nein danke|x|3|O|0|-|2|A0>NO>NO|shake 0.25 3",
      "f_yes|Ja genau|check|3|O|0|-|2|A0>YES>YES|nod 0.2 3",
      "f_cool|Cool bleiben|glasses|5|O|0|-|1|A0>SHADES>COOL|-",
      "f_bored2|Langeweile pur|hourglass|8|L|0|-|0.4|HIPS+LUP>HIPS+TL|-",
      "f_nervous|Nervös|pulse|6|L|0|-|5|PRAY>PRAY+TL|-",
      "f_relief|Erleichtert|sun|3|O|0|-|1|A0>FACE2>BROW+LUP|-",
      "f_jealous|Eifersüchtig|eye|5|O|0|-|1|A0>CROSS+LL>CROSS+LL|-",
      "f_love2|Herzklopfen|heart|5|L|0|heart|3|PRAY>PRAY+LUP|-",
      "f_kiss|Luftkuss|heart|3|O|0|heart|2|A0>MOUTHR>FWDR|-",
      "f_victory2|Siegerpose|trophy|4|O|0|-|1|A0>V+LUP>FLEX|-",
      "f_lose|Verloren|skull|4|O|0|cry|1|A0>SAD2>SAD2+LEANF|-",
      "f_stunned|Sprachlos|alert|4|O|0|-|2|A0>FACE2>FACE2|-",
      "f_oops|Hoppla|alert|3|O|0|-|2|A0>FACE2+TR>A0+TR|-",
      "f_annoyed|Genervt|cloud|4|O|0|-|1|A0>CROSS+LUP>CROSS+TL|-",
      "f_sneezy|Verschnupft|rain|4|O|0|-|1.5|A0>NOSE>NOSE+LDN|-",
      "f_giggle|Kichern|gg|4|L|0|laugh|5|MOUTHR+TL>MOUTHR+TR|-",
      "f_rofl|Vor Lachen biegen|gg|5|L|0|laugh|2|BENDF>LEANB+HAPPY|-",
      "f_hello2|Hallo zusammen|emote|3|O|0|-|2|A0>V>V|wave2 0.3 4",
      "f_welcome|Willkommen|home|3|O|0|-|1.2|A0>SHOW>SHOW|-",
      "f_thanks|Danke|heart|3|O|0|-|1|A0>PRAY+LDN>PRAY|-",
      "f_sorry|Entschuldigung|user|3|O|0|-|1|A0>BOW1>A0|-",
      "f_please|Bitte bitte|heart|4|L|0|-|3|PRAY>PRAY+LUP|-",
      "f_hungry|Hungrig|food|4|L|0|-|2|PRAY+LEANF>FACE2|-",
      "f_sleepy|Müde|moon|6|L|0|sleep|0.5|SLEEP>YAWN|-",
      "f_wakeup|Aufwachen|sun|4|O|0|-|1|SLEEP>STRETCH>A0|-",
      "f_hot|Schwitzen|sun|5|L|0|-|4|HOT+TL>HOT+TR|-",
      "f_freezing|Bibbern|snow|5|L|0|-|8|COLD>COLD+TL|-",
      "h_cape|Umhang wehen|wing|5|L|0|-|1|CAPE+LUP>CAPE+LEANB|-",
      "h_fly|Abheben|wing|4|O|0|-|1.5|A0>SQUAT+FWD2>FLY|-",
      "h_beam|Energiestrahl|bolt|4|O|0|-|1|CHARGE>CHARGE>BEAM>BEAM|-",
      "h_smash|Hulk-Schlag|hit|3|O|0|-|2|SMASH>POW>POW|-",
      "h_shield|Schild hoch|shield|4|O|0|-|2|A0>SHIELD>SHIELD|-",
      "h_sword2|Schwert ziehen|sword|3|O|0|-|1.5|A0>SWU>SWU+LUP|-",
      "h_slash|Doppelhieb|sword|3|O|0|-|3|SWU>SWD>SWU>SWD|-",
      "h_thrust|Ausfall|sword|2.6|O|0|-|2|GUARD>SWF+LUNGE>SWF+LUNGE|-",
      "h_spin|Wirbelangriff|sword|3|O|720|-|2|T>T>A0|-",
      "h_bow2|Schnellschuss|arrow|5|L|0|-|2|BOWD>BOWR|-",
      "h_throw|Speer werfen|arrow|2.6|O|0|-|2|THR1>THR2>A0|-",
      "h_ninja2|Ninja-Sprint|sprint|6|L|0|-|4|BACK2+RUN1+LEANF>BACK2+RUN2+LEANF|-",
      "h_karate2|Karate-Tritt|hit|2.4|O|0|-|2|GUARD>GUARD+SIDEKR>GUARD|-",
      "h_judo|Judo-Wurf|hit|3|O|0|-|1.5|FWD2>FWD2+TWL+LEANF>A0|-",
      "h_wrestle|Ringer|hit|6|L|0|-|1.5|GOALIE+LEANF>GOALIE+LEANF+TL|-",
      "h_flex2|Bodybuilder|trophy|6|L|0|-|0.7|FLEX>FLEXR>CROSS+SQUAT|-",
      "h_hero|Heldenpose|star|4|O|0|-|1|A0>PROUD+LUP>PROUD+LUP|-",
      "h_villain|Bösewicht|skull|5|L|0|-|1|VAMP>VAMP+LUP|-",
      "h_wizard2|Feuerball|flame|3|O|0|-|1.5|CHARGE>BEAM>BEAM|-",
      "h_witch|Hexe|wand|6|L|0|-|1|WITCH>WITCH+TL|-",
      "h_staff|Stab erheben|wand|4|O|0|-|1|A0>STAFF>STAFF|-",
      "h_king|König|crown|4|O|0|-|1|A0>CROWN>CROWNP|-",
      "h_knight|Ritterschlag|sword|4|O|0|-|1|A0>SWU>SWF+KNEE|-",
      "h_ghost|Gespenst|skull|6|L|0|-|1|GHOST+LEANL>GHOST+LEANR|-",
      "h_vampire|Vampir|moon|5|O|0|-|1|A0>VAMP>VAMP+LDN|-",
      "h_mummy2|Mumien-Gang|skull|6|L|0|-|1.5|MUMMY+WALK1>MUMMY+WALK2|-",
      "h_robot3|Cyborg|chip|6|L|0|-|1|BEAM+TWL>BEAM+TWR|-",
      "h_samurai|Samurai|sword|4|O|0|-|1|A0>BOWD>SWU+LUNGE|-",
      "h_viking|Wikinger|shield|5|L|0|-|2|SHIELD+SWU>SHIELD+SWD|-",
      "h_cowboy|Revolverheld|target|3|O|0|-|3|HIPS>POINTF>HIPS|-",
      "h_sniper|Scharfschütze|crosshair|4|O|0|-|1|A0>BINOC>FWD2+LEANF|-",
      "h_jedi|Lichtschwert|sword|5|L|0|-|1.5|SWU+GUARD>SWD>SWF|-",
      "h_force|Macht einsetzen|wand|4|O|0|-|1|A0>FWDR>FWDR+TL|-",
      "h_teleport|Teleport|sparkle|3|O|360|-|1|A0>UP2>A0|-",
      "h_shrink|Unsichtbar machen|eye|4|O|0|-|1|A0>PRAY+LDN>FACE2+SQUAT|-",
      "m_trumpet|Trompete|music|6|L|0|-|1|TRUMP>TRUMP+LUP|-",
      "m_drums2|Schlagzeug-Solo|music|6|L|0|-|6|DRUM1>DRUM2>CYMB>DRUM2|-",
      "m_violin2|Geigen-Solo|music|6|L|0|-|2|VIOL>VIOL2|tilt 0.1 1",
      "m_piano2|Konzertpianist|music|6|L|0|-|4|PIA1>PIA2|sway 0.1 0.7",
      "m_guitar2|Rockgitarre|music|6|L|0|-|6|GUIT1>GUIT2|nod 0.15 3",
      "m_sing|Singen|music|6|L|0|-|0.6|MIC>MICUP|sway 0.08 1",
      "m_rockstar|Rockstar|music|6|L|0|-|2|ROCK>MICUP|-",
      "m_flute|Flöte|music|6|L|0|-|2|FLUTE>FLUTE+TL|-",
      "m_dj2|DJ-Scratch|music|6|L|0|-|4|DJ>DJ+ROLL1|nod 0.15 2",
      "m_tambourine|Tamburin|music|6|L|0|-|4|HCLAP1>CLAP1|-",
      "m_maracas|Rasseln|music|6|L|0|-|6|DRUM1+UP2>DRUM2+UP2|-",
      "m_beatbox|Beatbox|music|6|L|0|-|4|MOUTHR>MOUTHR+TL|nod 0.15 4",
      "m_harmonica|Mundharmonika|music|6|L|0|-|1|MOUTHR+FACE2>FACE2+TR|-",
      "m_cello|Cello|music|6|L|0|-|1|VIOL2+SIT>VIOL+SIT|-",
      "m_choir|Chor|music|6|L|0|-|0.5|BOOK+LUP>BOOK|-",
      "m_conduct2|Dirigent (Finale)|music|4|O|0|-|2|CONDUCT>UP2>CROWN|-",
      "m_metal|Metal-Hörner|music|6|L|0|-|2|ROCK>UP2|nod 0.25 4",
      "m_clapbeat|Takt klatschen|music|6|L|0|-|2|HCLAP1>HCLAP2|-",
      "m_snap|Fingerschnipsen|music|6|L|0|-|2|SNAP>A0|bob 0.05 2",
      "m_air|Luftschlagzeug|music|6|L|0|-|4|DRUM1+LUP>DRUM2+LUP|-",
      "g_come|Komm her|users|3|O|0|-|3|A0>COME>COME2>COME>COME2|-",
      "g_stop2|Halt!|warning|3|O|0|-|2|A0>STOP2>STOP2|-",
      "g_point_me|Ich?|user|3|O|0|-|2|A0>POINTME>POINTME|-",
      "g_point_up|Da oben!|arrow|3|O|0|-|2|A0>POINTU+LUP>POINTU+LUP|-",
      "g_point_down|Da unten!|arrow|3|O|0|-|2|A0>POINTD>POINTD|-",
      "g_point_left|Nach links|arrow|3|O|0|-|2|A0>POINTSL>POINTSL|-",
      "g_point_right|Nach rechts|arrow|3|O|0|-|2|A0>POINTSR>POINTSR|-",
      "g_call|Rufen|sound|3|O|0|-|2|A0>CALL>CALL|-",
      "g_whisper|Flüstern|chat|3|O|0|-|1.5|A0>WHISPER>WHISPER|-",
      "g_secret|Geheimnis|lock|3|O|0|-|1.5|A0>SECRET>SECRET|-",
      "g_thumbs2|Doppel-Daumen|check|3|O|0|-|2|A0>THUMBS2>THUMBS2|-",
      "g_thumbdown|Daumen runter|x|3|O|0|-|2|A0>THUMBD>THUMBD|-",
      "g_bravo|Bravo|star|3|O|0|-|2|A0>BRAVO>CLAP2>BRAVO|-",
      "g_follow|Mir nach!|flag|3|O|0|-|2|A0>POINTF+LUNGE>UPR|-",
      "g_look|Schau mal|eye|3|O|0|-|2|A0>POINTSR+TR>POINTSR|-",
      "g_binoc|Fernglas|eye|5|L|0|-|0.5|BINOC+LL>BINOC+LR|-",
      "g_listen2|Hörst du das?|sound|4|O|0|-|1.5|A0>EARR>EARR+TL|-",
      "g_silence|Ruhe bitte|mute|3|O|0|-|2|A0>MOUTHR>STOP2|-",
      "g_timeout|Auszeit|clock|3|O|0|-|2|A0>CROSS+FWDR>CROSS|-",
      "g_okay2|Passt schon|check|3|O|0|-|2|A0>SNAP>A0|-",
      "g_maybe|Vielleicht|info|4|L|0|-|1|WHAT+TL>WHAT+TR|-",
      "g_idea|Idee!|bolt|3|O|0|-|2|A0>POINTU>POINTU+LUP|-",
      "g_count|Abzählen|number|4|O|0|-|3|A0>FWDR>POINTF>FWDR>POINTF|-",
      "g_rps|Schere-Stein-Papier|hit|3|O|0|-|3|FWDR>MOUTHR>FWDR>MOUTHR>POINTF|-",
      "g_handshake|Hand schütteln|users|3|O|0|-|5|A0>FWDR>POINTF>FWDR>POINTF|-",
      "g_bigwave|Großes Winken|emote|3|O|0|-|2|A0>UP2>UP2|wave2 0.5 3",
      "g_bye2|Bis bald|emote|3|O|0|-|2|A0>COME2>COME2|wave 0.35 4",
      "g_hug2|Gruppenumarmung|heart|3|O|0|heart|1|A0>T>CROSS|-",
      "g_cheerlead|Cheerleader|star|6|L|0|-|3|V>CROSS>UPR>UPL|-",
      "g_flag|Fahne schwenken|flag|6|L|0|-|1|UPR+LEANL>UPR+LEANR|wave 0.4 2",
      "l_brick|Stein aufsetzen|brick|3|O|0|-|1.5|A0>BRICK1>BRICK2|-",
      "l_build2|Lego bauen|brick|6|L|0|-|2|BRICK1>BRICK2|-",
      "l_stack|Turm stapeln|brick|4|O|0|-|1.5|BRICK2>BRICK1>BLOCKH|-",
      "l_minifig|Minifigur-Gang|lego|6|L|0|-|2|A0+MAR1>A0+MAR2|-",
      "l_minifig2|Minifigur-Gruß|lego|3|O|0|-|2|A0>FWDR>FWDR|-",
      "l_click|Klick!|brick|2.4|O|0|-|3|BRICK1>BRICK2>A0|-",
      "l_mine|Abbauen (schnell)|break|5|L|0|-|4|SWINGP>PICK2|-",
      "l_place|Block setzen|box|3|O|0|-|2|A0>PLACE>A0|-",
      "l_trade|Handeln|gem|4|O|0|-|1.5|A0>TRADE>TRADE+TL|-",
      "l_creeper|Creeper|warning|4|O|0|-|1|CREEP>CREEP+SQUAT>STAR|-",
      "l_enderman|Enderman|eye|6|L|0|-|0.4|ENDER>ENDER+TR|-",
      "l_totem|Totem-Rettung|totem|3|O|0|-|1.5|SQUAT+FWD2>TOTEM>V|-",
      "l_elytra|Elytra-Flug|wing|6|L|0|-|0.7|ELYTRA+LEANL>ELYTRA+LEANR|-",
      "l_shield|Schild blocken|shield|4|O|0|-|2|A0>SHIELDUP>SHIELDUP|-",
      "l_eat2|Steak essen|food|4|L|0|-|5|EAT>EAT+TL|-",
      "l_torch|Fackel halten|light|6|L|0|-|0.4|TORCH+LL>TORCH+LR|-",
      "l_sneak|Schleichen|sneak|6|L|0|-|2|SNEAK1>SNEAK2|-",
      "l_villager|Dorfbewohner|users|6|L|0|-|1|CROSS+LUP>CROSS+TL|-",
      "l_piglin|Piglin-Tanz|gem|6|L|0|-|3|GOLF1>GOLF2|-",
      "l_steve|Steve-Gang|user|6|L|0|-|2|WALK1>WALK2|-",
      "l_fishing|Angelrute auswerfen|rain|3|O|0|-|1.5|ROD1>ROD2>ROD2|-",
      "l_bucket|Eimer ausgießen|rain|3|O|0|-|1.5|A0>FWD2>POINTD|-",
      "l_bed|Ab ins Bett|moon|4|O|0|sleep|1|A0>YAWN>SLEEP|-",
      "l_respawn|Respawn|respawn|3|O|0|-|1.5|FAINT>A0>V|-",
      "l_fall|Hinfallen|skull|3|O|0|-|2|A0>FALL>FALL|-",
      "l_party|Party!|gift|6|L|0|-|3|CONF>FWD2|bob 0.06 3",
      "l_confetti|Konfetti werfen|sparkle|3|O|0|-|2|FWD2>CONF>CONF|-",
      "l_snowangel|Schneeengel|snow|6|L|0|-|1.5|STAR+LEANB>STAR0+LEANB|-",
      "l_snowman|Schneemann bauen|snow|6|L|0|-|1.5|SNOWA>CARRY+LEANF|-",
      "l_candle|Kerze auspusten|flame|3|O|0|-|1.5|A0>BLOW>BLOW2|-",
      "l_bubbles|Seifenblasen|sparkle|5|L|0|-|1|BLOW2>BLOW|-",
      "l_hang|Am Seil hängen|jump|6|L|0|-|1|HANG+LEANL>HANG+LEANR|-"
   };

   private EmotePack() {
   }

   private static void def(String name, String spec) {
      float[] p = new float[18];
      Arrays.fill(p, Float.NaN);

      for (String part : spec.split(";")) {
         String[] f = part.trim().split("\\s+");
         int idx = -1;

         for (int i = 0; i < PARTS.length; i++) {
            if (PARTS[i].equals(f[0])) {
               idx = i * 3;
            }
         }

         if (idx < 0) {
            throw new IllegalArgumentException("pose " + name + ": " + f[0]);
         }

         for (int k = 0; k < 3; k++) {
            p[idx + k] = Float.parseFloat(f[1 + k]);
         }
      }

      P.put(name, p);
   }

   private static float[] pose(String expr) {
      float[] out = new float[18];
      Arrays.fill(out, Float.NaN);

      for (String n : expr.split("\\+")) {
         float[] p = P.get(n.trim());
         if (p == null) {
            throw new IllegalArgumentException("unknown pose " + n);
         }

         for (int i = 0; i < out.length; i++) {
            if (!Float.isNaN(p[i])) {
               out[i] = p[i];
            }
         }
      }

      return out;
   }

   static void addAll() {
      for (String line : DATA) {
         String[] f = line.split("\\|", -1);
         String id = "p_" + f[0];
         float dur = Float.parseFloat(f[3]);
         boolean loop = f[4].equals("L");
         float spin = Float.parseFloat(f[5]);
         String bubble = f[6].equals("-") ? null : f[6];
         float speed = Float.parseFloat(f[7]);
         String[] names = f[8].split(">");
         List<float[]> seq = new ArrayList<>();

         for (String n : names) {
            seq.add(pose(n));
         }

         boolean[] used = new boolean[18];

         for (float[] p : seq) {
            for (int i = 0; i < p.length; i++) {
               if (!Float.isNaN(p[i])) {
                  used[i] = true;
               }
            }
         }

         for (float[] p : seq) {
            for (int ix = 0; ix < p.length; ix++) {
               if (used[ix] && Float.isNaN(p[ix])) {
                  p[ix] = 0.0F;
               }
            }
         }

         float[][] frames = seq.toArray(new float[0][]);
         List<float[]> fx = new ArrayList<>();
         if (!f[9].equals("-")) {
            for (String e : f[9].split(";")) {
               String[] g = e.trim().split("\\s+");
               fx.add(new float[]{fxType(g[0]), Float.parseFloat(g[1]), Float.parseFloat(g[2])});
            }
         }

         float[][] fxa = fx.toArray(new float[0][]);
         EmoteLib.addGenerated(id, f[1], f[2], dur, loop, bubble, spin, (t, p) -> play(frames, speed, loop, fxa, t, p));
      }
   }

   private static float fxType(String s) {
      return switch (s) {
         case "sway" -> 0.0F;
         case "twist" -> 1.0F;
         case "bob" -> 2.0F;
         case "nod" -> 3.0F;
         case "shake" -> 4.0F;
         case "tilt" -> 5.0F;
         case "legs" -> 6.0F;
         case "arms" -> 7.0F;
         case "wave" -> 8.0F;
         case "wave2" -> 9.0F;
         default -> throw new IllegalArgumentException("fx " + s);
      };
   }

   private static void play(float[][] fr, float speed, boolean loop, float[][] fx, float t, float[] p) {
      int n = fr.length;
      float[] a;
      float[] b;
      float k;
      if (n == 1) {
         a = b = fr[0];
         k = 0.0F;
      } else {
         float x = t * speed;
         if (loop) {
            int i = Math.floorMod((int)Math.floor(x), n);
            a = fr[i];
            b = fr[(i + 1) % n];
            k = x - (float)Math.floor(x);
         } else {
            x = Math.min(x, (float)(n - 1));
            int i = Math.min(n - 2, (int)Math.floor(x));
            a = fr[i];
            b = fr[i + 1];
            k = x - i;
         }

         k = k * k * (3.0F - 2.0F * k);
      }

      for (int i = 0; i < p.length; i++) {
         if (!Float.isNaN(a[i])) {
            p[i] = a[i] + (b[i] - a[i]) * k;
         }
      }

      for (float[] e : fx) {
         float v = e[1] * (float)Math.sin(t * e[2] * Math.PI * 2.0);
         switch ((int)e[0]) {
            case 0:
               addTo(p, 5, v);
               break;
            case 1:
               addTo(p, 4, v);
               break;
            case 2:
               addTo(p, 3, Math.abs(v));
               break;
            case 3:
               addTo(p, 0, v);
               break;
            case 4:
               addTo(p, 1, v);
               break;
            case 5:
               addTo(p, 2, v);
               break;
            case 6:
               addTo(p, 12, v);
               addTo(p, 15, -v);
               break;
            case 7:
               addTo(p, 6, v);
               addTo(p, 9, -v);
               break;
            case 8:
               addTo(p, 8, v);
               break;
            case 9:
               addTo(p, 8, v);
               addTo(p, 11, -v);
         }
      }
   }

   private static void addTo(float[] p, int i, float v) {
      p[i] = (Float.isNaN(p[i]) ? 0.0F : p[i]) + v;
   }

   static {
      def("N", "H 0 0 0;B 0 0 0;RA 0 0 0;LA 0 0 0;RL 0 0 0;LL 0 0 0");
      def("A0", "RA 0 0 0.12;LA 0 0 -0.12");
      def("UP2", "RA -2.9 0 0.25;LA -2.9 0 -0.25");
      def("UPR", "RA -2.9 0 0.2");
      def("UPL", "LA -2.9 0 -0.2");
      def("V", "RA -2.5 0 0.75;LA -2.5 0 -0.75");
      def("T", "RA 0 0 1.57;LA 0 0 -1.57");
      def("FWD2", "RA -1.57 0 0.05;LA -1.57 0 -0.05");
      def("FWDR", "RA -1.57 0 0");
      def("FWDL", "LA -1.57 0 0");
      def("HIPS", "RA -0.2 0 0.75;LA -0.2 0 -0.75");
      def("CROSS", "RA -1.25 0 -0.75;LA -1.15 0 0.75");
      def("PRAY", "RA -1.35 -0.35 -0.1;LA -1.35 0.35 0.1");
      def("GUARD", "RA -1.45 0 -0.55;LA -1.45 0 0.55");
      def("PUNR", "RA -1.6 0 -0.05;LA -1.0 0 0.45;B 0 -0.35 0");
      def("PUNL", "LA -1.6 0 0.05;RA -1.0 0 -0.45;B 0 0.35 0");
      def("BACK2", "RA 0.85 0 0.2;LA 0.85 0 -0.2");
      def("WINGD", "RA 0 0 0.9;LA 0 0 -0.9");
      def("WINGU", "RA 0 0 2.0;LA 0 0 -2.0");
      def("HEAD2", "RA -2.65 0 -0.6;LA -2.65 0 0.6");
      def("FACE2", "RA -2.0 0 -0.5;LA -2.0 0 0.5");
      def("MOUTHR", "RA -2.05 0 -0.55");
      def("EARR", "RA -2.5 0.3 -1.05");
      def("BROW", "RA -2.5 0 -0.8;H -0.1 0 0");
      def("BINOC", "RA -2.0 -0.2 -0.4;LA -2.0 0.2 0.4;H -0.1 0 0");
      def("STEER", "RA -1.2 0 -0.2;LA -1.2 0 0.2");
      def("STEERL", "RA -1.0 0 -0.2;LA -1.4 0 0.2");
      def("STEERR", "RA -1.4 0 -0.2;LA -1.0 0 0.2");
      def("PULL1", "RA -1.5 0 -0.1;LA -1.5 0 0.1;B 0.3 0 0");
      def("PULL2", "RA -0.5 0 -0.1;LA -0.5 0 0.1;B -0.15 0 0");
      def("CRAWL1", "RA -2.9 0 0.3;LA 0 0 -0.2");
      def("CRAWL2", "RA 0 0 0.2;LA -2.9 0 -0.3");
      def("RUN1", "RA 0.8 0 0;LA -0.8 0 0;RL -0.8 0 0;LL 0.8 0 0");
      def("RUN2", "RA -0.8 0 0;LA 0.8 0 0;RL 0.8 0 0;LL -0.8 0 0");
      def("MAR1", "RA -0.7 0 0.05;LA 0.5 0 -0.05;RL -1.1 0 0;LL 0.1 0 0");
      def("MAR2", "RA 0.5 0 0.05;LA -0.7 0 -0.05;RL 0.1 0 0;LL -1.1 0 0");
      def("KICKR", "RL -1.4 0 0;RA 0.5 0 0.2;LA -0.8 0 -0.2;B -0.15 0 0");
      def("KICKL", "LL -1.4 0 0;LA 0.5 0 -0.2;RA -0.8 0 0.2;B -0.15 0 0");
      def("SIDEKR", "RL 0 0 0.9;LA 0 0 -0.6;B 0 0 -0.15");
      def("SIDEKL", "LL 0 0 -0.9;RA 0 0 0.6;B 0 0 0.15");
      def("BACKKR", "RL 0.8 0 0;B 0.2 0 0");
      def("BACKKL", "LL 0.8 0 0;B 0.2 0 0");
      def("SQUAT", "B 0.45 0 0;RL -0.9 0 0.1;LL -0.9 0 -0.1");
      def("LUNGE", "RL -0.9 0 0;LL 0.6 0 0;B 0.1 0 0");
      def("LUNGEL", "LL -0.9 0 0;RL 0.6 0 0;B 0.1 0 0");
      def("LEANL", "B 0 0 0.25;H 0 0 0.2");
      def("LEANR", "B 0 0 -0.25;H 0 0 -0.2");
      def("LEANF", "B 0.4 0 0;H 0.25 0 0");
      def("LEANB", "B -0.25 0 0;H -0.45 0 0");
      def("TWL", "B 0 0.55 0;H 0 0.3 0");
      def("TWR", "B 0 -0.55 0;H 0 -0.3 0");
      def("LUP", "H -0.7 0 0");
      def("LDN", "H 0.6 0 0");
      def("LL", "H 0 0.9 0");
      def("LR", "H 0 -0.9 0");
      def("TL", "H 0 0 0.4");
      def("TR", "H 0 0 -0.4");
      def("H0", "H 0 0 0");
      def("B0", "B 0 0 0");
      def("L0", "RL 0 0 0;LL 0 0 0");
      def("SPLIT", "RL -1.4 0 0;LL 1.0 0 0");
      def("STAR", "RA 0 0 2.6;LA 0 0 -2.6;RL 0 0 0.45;LL 0 0 -0.45");
      def("STAR0", "RA 0 0 0.15;LA 0 0 -0.15;RL 0 0 0;LL 0 0 0");
      def("LEGW", "RL 0 0 0.35;LL 0 0 -0.35");
      def("KNEE", "RL -1.3 0 0;LL 0.3 0 0;B 0.15 0 0");
      def("SIT", "RL -1.5 0.1 0;LL -1.5 -0.1 0");
      def("TOER", "RL -0.35 0 0.1");
      def("TOEL", "LL -0.35 0 -0.1");
      def("DJ", "RA -1.3 0 -0.1;LA -2.2 0 0.75;H 0.25 0 0.15");
      def("TRUMP", "RA -1.75 0 -0.15;LA -1.75 0 0.15;H -0.25 0 0");
      def("DRUM1", "RA -1.25 0 -0.1;LA -0.6 0 0.1");
      def("DRUM2", "RA -0.6 0 -0.1;LA -1.25 0 0.1");
      def("CYMB", "RA -2.2 0 0.4;LA -0.6 0 0.1");
      def("VIOL", "LA -1.6 0.7 0;RA -1.25 0 -0.5;H 0 0.3 0.3");
      def("VIOL2", "LA -1.6 0.7 0;RA -1.0 0 -0.1;H 0 0.3 0.3");
      def("PIA1", "RA -1.0 0 -0.25;LA -1.0 0 0.25;B 0.15 0 0");
      def("PIA2", "RA -0.9 0 0.1;LA -1.1 0 -0.1;B 0.15 0 0");
      def("GUIT1", "RA -0.75 -0.4 -0.3;LA -1.2 0.6 0.2;H 0.2 -0.3 0");
      def("GUIT2", "RA -0.45 -0.4 -0.3;LA -1.2 0.6 0.2;H 0.3 -0.3 0");
      def("MIC", "RA -2.0 0 -0.4;H -0.15 0 0");
      def("MICUP", "RA -2.0 0 -0.4;LA -2.5 0 -0.6;H -0.4 0 0");
      def("FLUTE", "RA -1.7 -0.4 -0.2;LA -1.9 -0.6 0.3;H 0 -0.3 0");
      def("SWU", "RA -2.7 0 0.15;LA -0.3 0 -0.2");
      def("SWD", "RA -0.7 0 -0.5;B 0.2 -0.3 0");
      def("SWF", "RA -1.6 0 -0.05;LA -0.2 0 -0.3;B 0.1 0 0;RL -0.4 0 0");
      def("BOWD", "LA -1.57 0.3 0;RA -1.57 -0.6 0;H 0 0.35 0");
      def("BOWR", "LA -1.57 0.3 0;RA -1.4 0.1 0.6;H 0 0.35 0");
      def("THR1", "RA -2.6 0 0.4;B -0.1 0.3 0");
      def("THR2", "RA -1.1 0 -0.2;B 0.3 -0.2 0");
      def("ROD1", "RA -2.5 0 -0.2;LA -2.1 0 0.2");
      def("ROD2", "RA -1.3 0 -0.1;LA -1.1 0 0.1");
      def("DIG1", "RA -1.5 0 -0.1;LA -1.5 0 0.1;B 0.5 0 0");
      def("DIG2", "RA -0.3 0 -0.1;LA -0.3 0 0.1;B 0.1 0 0");
      def("HAM1", "RA -2.8 0 -0.1;LA -0.8 0 0.2");
      def("HAM2", "RA -1.0 0 -0.1;LA -0.8 0 0.2;B 0.2 0 0");
      def("SAW1", "RA -1.2 0 -0.2;B 0.25 0 0");
      def("SAW2", "RA -0.5 0 -0.2;B 0.15 0 0");
      def("PAINT1", "RA -2.2 0 -0.1");
      def("PAINT2", "RA -1.1 0 -0.1");
      def("STIR", "RA -1.2 0 -0.3;LA -0.9 0 0.3;H 0.35 0 0");
      def("TYPE", "RA -1.1 0 -0.25;LA -1.1 0 0.25;H 0.3 0 0");
      def("BOOK", "RA -1.25 0 -0.35;LA -1.25 0 0.35;H 0.45 0 0");
      def("PHONE", "RA -2.3 0 -0.95;H 0 0 0.2");
      def("TEXT", "RA -1.3 0 -0.4;LA -1.3 0 0.4;H 0.55 0 0");
      def("SWEEP1", "RA -0.9 0 -0.4;LA -0.5 0 0.1;B 0.2 0.3 0");
      def("SWEEP2", "RA -0.9 0 0.2;LA -0.5 0 0.4;B 0.2 -0.3 0");
      def("WASH", "RA -1.2 0 -0.2;LA -1.2 0 0.2;B 0.2 0 0;H 0.3 0 0");
      def("CARRY", "RA -1.4 0 -0.35;LA -1.4 0 0.35;B -0.15 0 0");
      def("BOXUP", "RA -2.9 0 0.1;LA -2.9 0 -0.1;H -0.3 0 0");
      def("WATCH", "LA -1.5 -0.5 0.3;H 0.35 0 0");
      def("TORCH", "RA -2.9 0 0.05;H -0.25 0 0");
      def("SHIELD", "LA -1.4 0.5 0.3;RA -0.4 0 0.2");
      def("CAPE", "RA 0 0 1.0;LA 0 0 -1.0;H -0.2 0 0");
      def("FLY", "RA -2.9 0 0.05;LA 0.4 0 -0.1;B 0.5 0 0;RL 0.3 0 0;LL 0.3 0 0");
      def("BEAM", "RA -1.55 -0.25 0;LA -1.55 0.25 0;B 0.1 0 0");
      def("CHARGE", "RA -0.8 0 0.6;LA -0.8 0 -0.6;B 0.25 0 0;H 0.2 0 0");
      def("SMASH", "RA -2.9 0 -0.2;LA -2.9 0 0.2;H -0.4 0 0");
      def("POW", "RA -1.4 0 -0.4;LA -1.4 0 0.4;B 0.45 0 0;H 0.3 0 0");
      def("FLEX", "RA 0 0 2.4;LA 0 0 -2.4;H -0.15 0 0");
      def("FLEXR", "RA 0 0 2.3;LA -0.2 0 -0.75;H 0 -0.4 0");
      def("CRAB", "RA -2.4 0 1.0;LA -2.4 0 -1.0;RL 0 0 0.35;LL 0 0 -0.35");
      def("PENG", "RA 0 0 0.4;LA 0 0 -0.4");
      def("TRUNK", "RA -1.6 0 -0.25;H 0.3 0 0");
      def("EARS", "RA -2.7 0 -0.35;LA -2.7 0 0.35");
      def("PAWS", "RA -1.35 0 -0.2;LA -1.35 0 0.2;H -0.1 0 0");
      def("CLAW", "RA -2.2 0 0.5;LA -2.2 0 -0.5;H -0.15 0 0;B 0.15 0 0");
      def("SNAKE", "RA -2.9 0 -0.15;LA -2.9 0 0.15");
      def("FISHB", "RA 0.3 0 0.25;LA 0.3 0 -0.25;B 0 0 0");
      def("FROG", "RA -0.5 0 0.6;LA -0.5 0 -0.6;B 0.45 0 0;RL -0.9 0 0.1;LL -0.9 0 -0.1");
      def("BEAR", "RA -2.4 0 0.3;LA -2.4 0 -0.3;H -0.2 0 0;B -0.1 0 0");
      def("OWL", "RA 0.1 0 0.3;LA 0.1 0 -0.3");
      def("DEER", "RA -2.9 0 0.6;LA -2.9 0 -0.6;H -0.1 0 0");
      def("SCORP", "RA -2.7 0 -0.4;LA -1.3 0 0.5;B 0.3 0 0");
      def("CRY2", "RA -2.05 0 -0.5;LA -2.05 0 0.5;H 0.4 0 0");
      def("SCARE", "RA -1.8 0 -0.4;LA -1.8 0 0.4;B -0.2 0 0;H -0.2 0 0");
      def("FISTS", "RA 0.1 0 0.3;LA 0.1 0 -0.3;H 0.3 0 0;B 0.15 0 0");
      def("SHY", "RA 0.3 0 -0.2;LA 0.3 0 0.2;H 0.4 0 0.2");
      def("PROUD", "RA -0.2 0 0.75;LA -0.2 0 -0.75;H -0.35 0 0;B -0.1 0 0");
      def("THUMBS2", "RA -1.5 0 -0.1;LA -1.5 0 0.1;H -0.1 0 0");
      def("THUMBD", "RA -0.9 0 -0.1;H 0.2 0 0");
      def("POINTF", "RA -1.6 0 -0.1");
      def("POINTU", "RA -3.0 0 0.05");
      def("POINTD", "RA -0.6 0 -0.1;H 0.4 0 0");
      def("POINTSR", "RA 0 0 1.5;H 0 -0.6 0");
      def("POINTSL", "LA 0 0 -1.5;H 0 0.6 0");
      def("POINTME", "RA -1.2 0 -0.75;H 0.2 0 0");
      def("COME", "RA -1.6 0 0.05;H 0 0 0");
      def("COME2", "RA -2.2 0 -0.3");
      def("STOP2", "RA -1.6 0 -0.1;LA -1.6 0 0.1;H -0.05 0 0");
      def("CALL", "RA -2.1 0 -0.25;LA -2.1 0 0.25;H -0.3 0 0");
      def("SELF", "RA -2.2 0.4 0.6;H -0.2 0.25 0");
      def("PICT", "RA -2.1 -0.2 -0.35;LA -2.1 0.2 0.35;H 0 0 0");
      def("BALL1", "RA -2.0 0 -0.3;LA -2.0 0 0.3;B 0.1 0 0");
      def("BALL2", "RA -2.95 0 -0.1;LA -2.6 0 0.2;H -0.3 0 0");
      def("GOLF1", "RA -1.9 0 1.0;LA -1.9 0 0.6;B 0 0.7 0;H 0.4 0 0");
      def("GOLF2", "RA -1.6 0 -0.6;LA -1.6 0 -0.9;B 0 -0.8 0;H 0.2 -0.4 0");
      def("TEN1", "RA 0.4 0 1.1;B 0 0.6 0");
      def("TEN2", "RA -1.6 0 -0.6;B 0 -0.5 0");
      def("BAT1", "RA -2.2 0 0.9;LA -2.2 0 0.6;B 0 0.6 0");
      def("BAT2", "RA -1.5 0 -0.5;LA -1.5 0 -0.8;B 0 -0.7 0");
      def("GOALIE", "RA -2.2 0 1.0;LA -2.2 0 -1.0;B 0.45 0 0;RL -0.9 0 0.1;LL -0.9 0 -0.1");
      def("DIVE", "RA -2.9 0 0.1;LA -2.9 0 -0.1;B 0.6 0 0;RL 0.4 0 0;LL 0.4 0 0");
      def("SKATE1", "B 0.35 0 0;RA 0.8 0 0.2;LA -0.8 0 -0.2;RL 0.5 0 0.2;LL -0.2 0 0");
      def("SKATE2", "B 0.35 0 0;RA -0.8 0 0.2;LA 0.8 0 -0.2;RL -0.2 0 0;LL 0.5 0 -0.2");
      def("LIFT1", "RA -1.3 0 0.4;LA -1.3 0 -0.4;B 0.45 0 0;RL -0.9 0 0.1;LL -0.9 0 -0.1");
      def("LIFT2", "RA -3.0 0 0.35;LA -3.0 0 -0.35;H -0.2 0 0");
      def("JAV", "RA -2.4 0 0.3;LA -1.2 0 -0.3;B -0.2 0.3 0");
      def("SPRINT0", "RA 0.3 0 0.1;LA -0.3 0 -0.1;B 0.5 0 0;RL -0.5 0 0;LL 0.4 0 0");
      def("DISC1", "RA -2.6 0 0.6;LA 0.3 0 -0.1");
      def("DISC2", "RA 0.35 0 -0.4;LA 0.3 0 -0.1");
      def("YMC_Y", "RA -2.55 0 0.7;LA -2.55 0 -0.7");
      def("YMC_M", "RA -2.65 0 -0.6;LA -2.65 0 0.6");
      def("YMC_C", "RA -2.6 0 0.2;LA -1.0 0 -1.1");
      def("YMC_A", "RA -2.85 0 -0.25;LA -2.85 0 0.25");
      def("EGY1", "RA -1.57 0 1.0;LA 0.3 0 -0.6;H 0 -0.4 0");
      def("EGY2", "LA -1.57 0 -1.0;RA 0.3 0 0.6;H 0 0.4 0");
      def("SPRK", "LA -2.4 0 0.9;RA -1.57 -0.4 0.6");
      def("SPRK2", "LA -2.4 0 0.9;RA -1.57 0.4 0.6");
      def("RAISE1", "RA -2.95 0 0.3;LA -2.95 0 -0.3");
      def("RAISE2", "RA -2.3 0 0.55;LA -2.3 0 -0.55");
      def("CLAP1", "RA -1.35 0 -0.05;LA -1.35 0 0.05");
      def("CLAP2", "RA -1.35 0 0.5;LA -1.35 0 -0.5");
      def("HCLAP1", "RA -2.8 0 -0.05;LA -2.8 0 0.05");
      def("HCLAP2", "RA -2.8 0 0.45;LA -2.8 0 -0.45");
      def("SNAP", "RA -1.2 0 0.4;LA -1.2 0 -0.4");
      def("WAVEL", "RA 0 0 1.3;LA 0 0 -1.8");
      def("WAVER", "RA 0 0 1.8;LA 0 0 -1.3");
      def("ROLL1", "RA -1.4 0 -0.3;LA -1.2 0 0.3");
      def("ROLL2", "RA -1.2 0 -0.3;LA -1.4 0 0.3");
      def("HULA1", "RA 0 0 1.25;LA 0 0 -0.5;B 0 0 0.15");
      def("HULA2", "RA 0 0 0.5;LA 0 0 -1.25;B 0 0 -0.15");
      def("SWIM1", "RA -2.9 0 0.25;LA -2.9 0 -0.25;B 0.3 0 0");
      def("SWIM2", "RA 0.3 0 1.2;LA 0.3 0 -1.2;B 0.3 0 0");
      def("ROCK", "RA -2.9 0 0.3;LA -0.3 0 -0.1;H 0.35 0 0");
      def("ZEN", "RA -0.6 0 0.5;LA -0.6 0 -0.5;H -0.1 0 0");
      def("SLEEP", "H 0.5 0 0.3;B 0.1 0 0;RA 0.05 0 0.05;LA 0.05 0 -0.05");
      def("YAWN", "RA -2.9 0 0.5;LA -2.9 0 -0.5;H -0.45 0 0");
      def("STRETCH", "RA -3.0 0 -0.15;LA -3.0 0 0.15;B -0.15 0 0");
      def("BENDF", "B 0.8 0 0;H 0.4 0 0;RA -0.8 0 0;LA -0.8 0 0");
      def("WALK1", "RA 0.4 0 0.05;LA -0.4 0 -0.05;RL -0.45 0 0;LL 0.45 0 0");
      def("WALK2", "RA -0.4 0 0.05;LA 0.4 0 -0.05;RL 0.45 0 0;LL -0.45 0 0");
      def("TIPTOE", "RA 0.2 0 0.4;LA 0.2 0 -0.4;B 0.15 0 0;H 0.2 0 0");
      def("SNEAK1", "RA -1.0 0 0.3;LA -0.6 0 -0.3;B 0.35 0 0;RL -0.5 0 0;LL 0.3 0 0");
      def("SNEAK2", "RA -0.6 0 0.3;LA -1.0 0 -0.3;B 0.35 0 0;RL 0.3 0 0;LL -0.5 0 0");
      def("MUMMY", "RA -1.57 0 0;LA -1.57 0 0;H 0.15 0 0");
      def("GHOST", "RA -1.7 0 0.25;LA -1.7 0 -0.25;H -0.2 0 0.2");
      def("VAMP", "RA -1.8 0 -0.9;LA -0.2 0 -0.2;H 0.1 0.4 0");
      def("WITCH", "RA -2.2 0.3 0.2;LA -1.8 -0.3 -0.2;B 0.2 0 0;H 0.2 0 0");
      def("CROWN", "RA -2.6 0 -0.5;LA -2.6 0 0.5;H -0.2 0 0");
      def("WAND", "RA -2.0 0 -0.2;LA -0.3 0 -0.2");
      def("WANDS", "RA -2.7 0 0.4;LA -0.3 0 -0.2");
      def("STAFF", "RA -1.1 0 -0.1;LA -2.6 0 -0.4;H -0.3 0 0");
      def("CHEF", "RA -1.3 0 -0.2;LA -1.3 0 0.4;H 0.3 0 0");
      def("WAITER", "RA -2.0 0 0.9;LA -0.3 0 -0.1;H -0.1 0 0");
      def("POLICE", "RA -1.6 0 -0.1;LA -0.2 0 -0.75");
      def("COP2", "RA 0 0 1.57;LA -0.2 0 -0.75;H 0 -0.5 0");
      def("PILOT", "RA 0 0 1.4;LA 0 0 -1.4;B 0 0 0.2");
      def("SCIENT", "RA -1.6 0 -0.2;LA -1.9 0 0.2;H -0.1 0.2 0");
      def("DOC", "RA -1.4 0 -0.1;LA -1.0 0 0.3;H 0.3 0 0");
      def("BUILD", "RA -2.1 0 -0.2;LA -1.4 0 0.2;H -0.3 0 0");
      def("TEACH", "RA -2.3 0 0.3;LA -0.9 0 0.2;H 0 -0.3 0");
      def("FARM", "RA -1.0 0 -0.2;LA -1.6 0 0.2;B 0.35 0 0");
      def("ASTRO", "RA -1.0 0 0.5;LA -1.0 0 -0.5;B -0.1 0 0;H -0.3 0 0");
      def("PIRATE", "RA -1.8 0 0.1;LA -0.2 0 -0.75;H -0.15 0 0");
      def("SPY", "RA -1.2 0 -0.4;LA -1.2 0 0.4;B 0.3 0 0;H 0.1 0 0");
      def("CONDUCT", "RA -2.0 0 0.3;LA -1.6 0 -0.4;H -0.1 0 0");
      def("BRICK1", "RA -1.6 0 -0.35;LA -1.6 0 0.35;H 0.35 0 0;B 0.15 0 0");
      def("BRICK2", "RA -1.1 0 -0.35;LA -1.1 0 0.35;H 0.45 0 0;B 0.25 0 0");
      def("BLOCKH", "RA -2.95 0 -0.05;LA -2.95 0 0.05;H -0.2 0 0");
      def("SWINGP", "RA -2.4 0 -0.3;LA -0.5 0 0.1;B 0.2 0 0");
      def("PICK2", "RA -0.6 0 -0.3;LA -0.5 0 0.1;B 0.35 0 0");
      def("TRADE", "RA -1.4 0 -0.3;LA -1.4 0 0.3;H 0.2 0 0");
      def("PLACE", "RA -1.2 0 -0.1;H 0.6 0 0;B 0.25 0 0");
      def("CREEP", "RA 0 0 0.05;LA 0 0 -0.05;H 0.2 0 0;B 0.1 0 0");
      def("ENDER", "RA -0.3 0 0.25;LA -0.3 0 -0.25;H 0 0 0.6");
      def("TOTEM", "RA -1.5 0 -0.1;LA -1.5 0 0.1;H -0.6 0 0");
      def("ELYTRA", "RA 0.2 0 0.9;LA 0.2 0 -0.9;B 0.6 0 0;RL 0.4 0 0;LL 0.4 0 0");
      def("SHIELDUP", "LA -1.6 0.6 0.4;RA -0.6 0 0.2;B 0.1 0 0");
      def("EAT", "RA -1.9 0 -0.6;H 0.2 0 0");
      def("CHEERS", "RA -2.5 0 -0.2;H -0.2 0 0");
      def("CAKE", "RA -1.4 0 -0.3;LA -1.4 0 0.3;H 0.4 0 0");
      def("BALLOON", "RA -2.95 0 0.05;H -0.6 0 0");
      def("PRESENT", "RA -1.6 0 -0.25;LA -1.6 0 0.25;H 0.1 0 0");
      def("CONF", "RA -2.7 0 0.6;LA -2.7 0 -0.6;H -0.35 0 0");
      def("SNOWA", "RA -1.0 0 -0.3;LA -1.0 0 0.3;B 0.6 0 0;H 0.3 0 0");
      def("HOT", "RA -1.6 0.4 0.4;LA -1.6 -0.4 -0.4;H 0.2 0 0");
      def("COLD", "RA -1.2 0 -0.75;LA -1.1 0 0.75;H 0.3 0 0");
      def("UMBR", "RA -2.4 0 -0.2;H -0.1 0 0");
      def("KITE", "RA -2.5 0 -0.1;LA -2.3 0 0.1;H -0.6 0 0");
      def("JUMPP", "RA -2.9 0 0.5;LA -2.9 0 -0.5;RL -0.5 0 0.2;LL -0.5 0 -0.2;H -0.3 0 0");
      def("HANG", "RA -3.0 0 -0.1;LA -3.0 0 0.1;RL 0.1 0 0;LL 0.1 0 0;H -0.4 0 0");
      def("SHADES", "RA -2.1 0 -0.6;LA -2.1 0 0.6;H -0.1 0 0");
      def("COOL", "RA -1.25 0 -0.75;LA -1.15 0 0.75;H -0.2 0.3 0.15;B -0.1 0 0");
      def("MONEY", "RA -1.7 0 -0.2;LA -0.4 0 0.2;H -0.15 0 0");
      def("CROWNP", "RA -2.9 0 0;H -0.2 0 0");
      def("BLOW", "RA -2.1 0 -0.45;H 0 0 0");
      def("BLOW2", "RA -1.5 0 -0.15;H -0.1 0 0");
      def("MIRROR", "RA -1.7 0 -0.2;H 0.1 0 0.3");
      def("HAIR", "RA -2.7 0.2 -0.7;H -0.2 0 -0.3");
      def("NOSE", "RA -2.05 0 -0.4;H -0.3 0 0");
      def("SAD2", "H 0.7 0 0;RA 0.15 0 0.05;LA 0.15 0 -0.05;B 0.15 0 0");
      def("HAPPY", "RA -2.5 0 0.5;LA -2.5 0 -0.5;H -0.3 0 0");
      def("BOW1", "B 0.6 0 0;H 0.5 0 0;RA -0.2 0 0;LA 0.5 0 0");
      def("OVER", "B -0.2 0 0;H -0.5 0 0;RA 0.2 0 0.9;LA 0.2 0 -0.9");
      def("FALL", "B -0.4 0 0;H -0.6 0 0;RA -2.6 0 0.9;LA -2.6 0 -0.9;RL -0.6 0 0;LL -0.2 0 0");
      def("DIZZY", "H 0.2 0 0.4;RA 0.3 0 0.6;LA 0.3 0 -0.6");
      def("FAINT", "B 0.5 0 0.3;H 0.7 0 0.4;RA 0.2 0 0.4;LA 0.2 0 -0.2");
      def("HICCUP", "H -0.25 0 0;B -0.1 0 0");
      def("WAIT", "RA 0.3 0 0.15;LA -1.5 -0.5 0.3;H 0.35 0 0");
      def("SHOW", "RA -1.4 0 0.9;LA -1.4 0 -0.9;H -0.1 0 0");
      def("TADA", "RA -2.6 0 1.0;LA -2.6 0 -1.0;H -0.2 0 0;RL -0.2 0 0.15");
      def("WHAT", "RA -0.7 0.9 1.15;LA -0.7 -0.9 -1.15;H 0 0 0.3");
      def("NO", "RA -1.6 0 -0.6;LA -1.6 0 0.6;H 0 0 0");
      def("YES", "RA -1.6 0 0.1;H 0.2 0 0");
      def("WHISPER", "RA -2.1 0.4 -0.8;H 0 -0.4 0.2;B 0 0 -0.15");
      def("SECRET", "RA -2.05 0 -0.55;LA -0.9 0 0.3;H 0.15 0 0");
      def("BRAVO", "RA -1.6 0 -0.25;LA -1.6 0 0.25;H -0.2 0 0");
   }
}
