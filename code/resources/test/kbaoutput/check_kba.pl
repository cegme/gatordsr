#!/usr/bin/perl -w

use strict;
use JSON;

# Check a TREC 2013 KBA track submission for various
# common errors:
#      * extra fields
#      * multiple run tags/team ids
#      * extraneous topics
#      * invalid retrieved documents (approximate check)
#      * no documents retrieved for an entity (warning only)
# Messages regarding submission are printed to an error log

# Results input file is in the form
#     first-line comment containing JSON description of run
#     team-id tag doc-id topic-string score level mention date-hour slot equiv-class byte-range
# where
# 	score must be an integer in (0,1000] 
#	level is exactly one of -1, 0, 1, 2 (only 2 participates in scoring)
#	mention is either 0 or 1
#	slot is either 'NULL' (for CCR) or a string that begins with
#		either 'FAC' 'PER' or 'ORG'
#	equiv-class is -1 for CCR and system-generated otherwise
#	byte range is 0-0 for CCR and an inclusive byte range otherwise

# Arbitrary comment lines begin with a # and may appear anywhere
# after the first line; they are ignored
# The first line *must* be a comment that contains a valid JSON description
# of the run *according to the filter-run.json schema).

# Change here put error log in some other directory
my $errlog_dir = ".";

# If more than 25 errors, then stop processing; something drastically
# wrong with the file.
my $MAX_ERRORS = 25; 

my %topics = ("http://en.wikipedia.org/wiki/Aharon_Barak", 0,
	"http://en.wikipedia.org/wiki/Alex_Kapranos", 0,
	"http://en.wikipedia.org/wiki/Alexander_McCall_Smith", 0,
	"http://en.wikipedia.org/wiki/Annie_Laurie_Gaylor", 0,
        "http://en.wikipedia.org/wiki/Basic_Element_(company)", 0,
	"http://en.wikipedia.org/wiki/Basic_Element_(music_group)", 0,
	"http://en.wikipedia.org/wiki/Bill_Coen", 0,
	"http://en.wikipedia.org/wiki/Boris_Berezovsky_(businessman)", 0,
	"http://en.wikipedia.org/wiki/Boris_Berezovsky_(pianist)", 0,
	"http://en.wikipedia.org/wiki/Charlie_Savage", 0,
	"http://en.wikipedia.org/wiki/Darren_Rowse", 0,
	"http://en.wikipedia.org/wiki/Douglas_Carswell", 0,
	"http://en.wikipedia.org/wiki/Frederick_M._Lawrence", 0,
	"http://en.wikipedia.org/wiki/Ikuhisa_Minowa", 0,
	"http://en.wikipedia.org/wiki/James_McCartney", 0, 
	"http://en.wikipedia.org/wiki/Jim_Steyer", 0,
	"http://en.wikipedia.org/wiki/Lisa_Bloom", 0,
	"http://en.wikipedia.org/wiki/Lovebug_Starski", 0,
	"http://en.wikipedia.org/wiki/Mario_Garnero", 0, 
	"http://en.wikipedia.org/wiki/Masaru_Emoto", 0,
	"http://en.wikipedia.org/wiki/Nassim_Nicholas_Taleb", 0, 
	"http://en.wikipedia.org/wiki/Rodrigo_Pimentel", 0, 
	"http://en.wikipedia.org/wiki/Roustam_Tariko", 0,
	"http://en.wikipedia.org/wiki/Ruth_Rendell", 0,
	"http://en.wikipedia.org/wiki/Satoshi_Ishii", 0,
	"http://en.wikipedia.org/wiki/Vladimir_Potanin", 0,
 	"http://en.wikipedia.org/wiki/William_Cohen", 0,
	"http://en.wikipedia.org/wiki/William_D._Cohan",0,
	"http://en.wikipedia.org/wiki/William_H._Gates,_Sr", 0,
	"http://en.wikipedia.org/wiki/Stuart_Powell_Field", 0,
	"https://twitter.com/CorbinSpeedway",0,
	"http://en.wikipedia.org/wiki/IDSIA",0,
	"http://en.wikipedia.org/wiki/Corn_Belt_Power_Cooperative",0,
	"http://en.wikipedia.org/wiki/Fargo-Moorhead_Symphony_Orchestra", 0,
	"http://en.wikipedia.org/wiki/Fargo_Air_Museum", 0,
	"http://en.wikipedia.org/wiki/Hjemkomst_Center", 0,
	"http://en.wikipedia.org/wiki/John_D._Odegard_School_of_Aerospace_Sciences", 0,
	"http://en.wikipedia.org/wiki/Red_River_Zoo", 0,
	"http://en.wikipedia.org/wiki/Star_Lite_Motel", 0,
	"http://en.wikipedia.org/wiki/Elysian_Charter_School", 0,
	"http://en.wikipedia.org/wiki/Stevens_Cooperative_School", 0,
	"http://en.wikipedia.org/wiki/Weehawken_Cove", 0,
	"http://en.wikipedia.org/wiki/Copper_Basin_Railway", 0,
	"http://en.wikipedia.org/wiki/Hayden_Smelter", 0,
	"http://en.wikipedia.org/wiki/Lewis_and_Clark_Landing", 0,
	"http://en.wikipedia.org/wiki/Toquepala_mine", 0,
	"http://en.wikipedia.org/wiki/Appleton_Museum_of_Art", 0,
	"http://en.wikipedia.org/wiki/Don_Garlits_Museum_of_Drag_Racing", 0,
	"http://en.wikipedia.org/wiki/Eighth_Street_Elementary_School", 0,
	"http://en.wikipedia.org/wiki/Lake_Weir_High_School", 0,
	"http://en.wikipedia.org/wiki/Marion_Technical_Institute", 0,
	"http://en.wikipedia.org/wiki/Osceola_Middle_School", 0,
	"http://en.wikipedia.org/wiki/The_Ritz_Apartment_(Ocala,_Florida)", 0,
	"http://en.wikipedia.org/wiki/Great_American_Brass_Band_Festival", 0,
	"http://en.wikipedia.org/wiki/Innovis_Health", 0,
	"http://en.wikipedia.org/wiki/Hoboken_Reporter", 0,
	"http://en.wikipedia.org/wiki/Hoboken_Volunteer_Ambulance_Corps", 0,
	"http://en.wikipedia.org/wiki/Agroindustrial_Pomalca", 0,
	"http://en.wikipedia.org/wiki/Atacocha", 0,
	"http://en.wikipedia.org/wiki/Austral_Group", 0,
	"http://en.wikipedia.org/wiki/Cementos_Lima", 0,
	"http://en.wikipedia.org/wiki/Dunkelvolk", 0,
	"http://en.wikipedia.org/wiki/Gran%C3%A3_y_Montero", 0,
	"http://en.wikipedia.org/wiki/Intergroup_Financial_Services", 0,
	"http://en.wikipedia.org/wiki/Luz_del_Sur", 0,
	"http://en.wikipedia.org/wiki/SIMSA", 0,
	"http://en.wikipedia.org/wiki/Scotiabank_Per%C3%BA", 0,
	"https://twitter.com/BlossomCoffee", 0,
	"https://twitter.com/FrankandOak", 0,
	"https://twitter.com/GandBcoffee",0,
	"https://twitter.com/WCoffeeResearch",0,
	"https://twitter.com/evvnt", 0,
	"http://en.wikipedia.org/wiki/Benjamin_Bronfman", 0,
	"http://en.wikipedia.org/wiki/Charles_Bronfman", 0,
	"http://en.wikipedia.org/wiki/Clare_Bronfman", 0,
	"http://en.wikipedia.org/wiki/Edgar_Bronfman,_Jr.", 0,
	"http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr.", 0,
	"http://en.wikipedia.org/wiki/Phyllis_Lambert", 0,
	"http://en.wikipedia.org/wiki/Sara_Bronfman", 0,
	"http://en.wikipedia.org/wiki/DeAnne_Smith", 0,
	"https://twitter.com/RonFunches", 0,
	"https://twitter.com/roryscovel", 0,
	"https://twitter.com/AlexJoHamilton", 0,
	"https://twitter.com/BobStovall", 0,
	"https://twitter.com/RobCaud", 0,
	"https://twitter.com/danvillekyengr", 0,
	"https://twitter.com/tonyg203", 0,
	"https://twitter.com/urbren00", 0,
	"http://en.wikipedia.org/wiki/Geoffrey_E._Hinton", 0,
	"http://en.wikipedia.org/wiki/L%C3%A9on_Bottou", 0,
	"http://en.wikipedia.org/wiki/Yann_LeCun", 0,
	"http://en.wikipedia.org/wiki/Zoubin_Ghahramani", 0,
	"http://en.wikipedia.org/wiki/Blair_Thoreson", 0,
	"http://en.wikipedia.org/wiki/Brenda_Weiler", 0,
	"http://en.wikipedia.org/wiki/Carey_McWilliams_(marksman)", 0,
	"http://en.wikipedia.org/wiki/Clark_Blaise", 0,
	"http://en.wikipedia.org/wiki/Daniel_J._Crothers", 0,
	"http://en.wikipedia.org/wiki/Danny_Irmen", 0,
	"http://en.wikipedia.org/wiki/David_B._Danbom", 0,
	"http://en.wikipedia.org/wiki/Drew_Wrigley", 0,
	"http://en.wikipedia.org/wiki/Ed_Bok_Lee", 0,
	"http://en.wikipedia.org/wiki/Fargo_Moorhead_Derby_Girls", 0,
	"http://en.wikipedia.org/wiki/George_Sinner", 0,
	"http://en.wikipedia.org/wiki/Gretchen_Hoffman", 0,
	"http://en.wikipedia.org/wiki/Jamie_Parsley", 0,
	"http://en.wikipedia.org/wiki/Jasper_Schneider", 0,
	"http://en.wikipedia.org/wiki/Jeff_Severson", 0,
	"http://en.wikipedia.org/wiki/Jennifer_Baumgardner", 0,
	"http://en.wikipedia.org/wiki/Jim_Poolman", 0,
	"http://en.wikipedia.org/wiki/John_H._Lang", 0,
	"http://en.wikipedia.org/wiki/Joshua_Boschee", 0,
	"http://en.wikipedia.org/wiki/Ken_Fowler", 0,
	"http://en.wikipedia.org/wiki/Keri_Hehn", 0,
	"http://en.wikipedia.org/wiki/Olaus_Murie", 0,
	"http://en.wikipedia.org/wiki/Paul_Johnsgard", 0,
	"http://en.wikipedia.org/wiki/Paul_Marquart", 0,
	"http://en.wikipedia.org/wiki/Richard_Edlund", 0,
	"http://en.wikipedia.org/wiki/Richard_W._Goldberg", 0,
	"http://en.wikipedia.org/wiki/Susan_Krieg", 0,
	"http://en.wikipedia.org/wiki/William_P._Gerberding", 0,
	"http://en.wikipedia.org/wiki/Ana%C3%AFs_Croze", 0,
	"http://en.wikipedia.org/wiki/Gwena%C3%ABlle_Aubry", 0,
	"http://en.wikipedia.org/wiki/Nicolas_Sch%C3%B6ffer", 0,
	"http://en.wikipedia.org/wiki/Th%C3%A9o_Mercier", 0,
	"http://en.wikipedia.org/wiki/Chiara_Nappi", 0,
	"http://en.wikipedia.org/wiki/Eva_Silverstein", 0,
	"http://en.wikipedia.org/wiki/Matt_Witten", 0,
	"http://en.wikipedia.org/wiki/Shamit_Kachru", 0,
	"http://en.wikipedia.org/wiki/Angelo_Savoldi", 0,
	"http://en.wikipedia.org/wiki/Bernard_Kenny", 0,
	"http://en.wikipedia.org/wiki/Bob_Bert", 0,
	"http://en.wikipedia.org/wiki/Carl_Chang_(tennis)", 0,
	"http://en.wikipedia.org/wiki/Carla_Katz", 0,
	"http://en.wikipedia.org/wiki/Derrick_Alston", 0,
	"http://en.wikipedia.org/wiki/Frank_Winters", 0,
	"http://en.wikipedia.org/wiki/Henry_Gutierrez", 0,
	"http://en.wikipedia.org/wiki/Jack_Lazorko", 0,
	"http://en.wikipedia.org/wiki/Jeff_Tamarkin", 0,
	"http://en.wikipedia.org/wiki/Joanne_Borgella", 0,
	"http://en.wikipedia.org/wiki/Ken_Freedman", 0,
	"http://en.wikipedia.org/wiki/Klaus_Grutzka", 0,
	"http://en.wikipedia.org/wiki/Mark_SaFranko", 0,
	"http://en.wikipedia.org/wiki/Maurice_Fitzgibbons", 0,
	"http://en.wikipedia.org/wiki/Pat_Dapuzzo", 0, 
	"http://en.wikipedia.org/wiki/Ruben_J._Ramos", 0,
	"http://en.wikipedia.org/wiki/Tilo_Rivas", 0,
	"http://en.wikipedia.org/wiki/William_H._Miller_(writer)", 0,
	"http://en.wikipedia.org/wiki/Buddy_MacKay", 0,
	"http://en.wikipedia.org/wiki/Chuck_Pankow", 0,
	"http://en.wikipedia.org/wiki/Haven_Denney", 0,
	"http://en.wikipedia.org/wiki/Jeremy_McKinnon", 0,
	"http://en.wikipedia.org/wiki/Joey_Mantia", 0,
	"http://en.wikipedia.org/wiki/Judd_Davis", 0,
	"http://en.wikipedia.org/wiki/Lorenzo_Williams_(basketball)", 0,
	"http://en.wikipedia.org/wiki/Randy_Ewers", 0,
	"http://en.wikipedia.org/wiki/Reid_Nichols", 0,
	"http://en.wikipedia.org/wiki/Scot_Brantley", 0,
	"http://en.wikipedia.org/wiki/Sean_Hampton", 0,
	"http://en.wikipedia.org/wiki/Travis_Mays", 0,
	"https://twitter.com/BartowMcDonald", 0,
	"https://twitter.com/KentGuinn4Mayor", 0,
	"https://twitter.com/bobplaisted", 0,
	"https://twitter.com/redmondmusic", 0,
	"https://twitter.com/sandrafriend", 0,
	"http://en.wikipedia.org/wiki/Joshua_Zetumer", 0,
	"https://twitter.com/MissMarcel", 0,
	"http://en.wikipedia.org/wiki/Barbara_Liskov", 0,
	"http://en.wikipedia.org/wiki/Fernando_J._Corbat%C3%B3", 0,
	"http://en.wikipedia.org/wiki/Juris_Hartmanis", 0,
	"http://en.wikipedia.org/wiki/Shafi_Goldwasser", 0,
);

my $results_file;		# input file to be checked (input param)
my $line;			# current input line
my $line_num;			# current input line number
my $errlog;			# file name of error log
my $num_errors;			
my $topic;
my ($team,$date_hour,$docno,$sim,$tag,$rel_level,$mention,$slot,$equiv,$bytes);
my ($docid);
my ($run_id,$team_id);
my $item;
my ($hour,$day,$month,$year);
my ($task_id);

my $usage = "Usage: $0 resultsfile\n";
$#ARGV == 0 || die $usage;
$results_file = $ARGV[0];
open RESULTS, "<$results_file" ||
	die "Unable to open results file $results_file: $!\n";

my @path = split "/", $results_file;
my $base = pop @path;
$errlog = $errlog_dir . "/" . $base . ".errlog";
open ERRLOG, ">$errlog" ||
	die "Cannot open error log for writing\n";
$num_errors = 0;
$line_num = 0;


# First process description of run
my ($json,$hashref,$k);
$run_id = ""; $team_id= "";
$line = <RESULTS> || die "empty input file";
chomp $line;
if (1 != ($line =~ s/^\s*#//)) {
    &error("First line not a comment (must be JSON description of run)");
    exit 255;
}
$json = JSON->new->allow_nonref();
eval {$hashref = $json->decode($line);};
if ($@) {
    &error("Parsing of JSON description failed: $@");
    exit 255;
}
foreach $k ("system_id", "team_id", "team_name",
		"system_description", "system_description_short",
		"run_type", "corpus_id",
		"topic_set_id", "task_id", "poc_name", "poc_email") {
    if (! defined $hashref->{$k}) {
	&error("JSON run description is missing required element '$k'");
    }
}

$run_id = $hashref->{"system_id"};
if ($run_id !~ /[A-Za-z0-9_.]{1,15}/) {
    &error("Invalid system-id '$run_id': system-ids must be between 1-15 characters consisting of only letters, numbers, '_' and '.'");
}

$team_id = $hashref->{"team_id"};

$item = $hashref->{"run_type"};
if ($item ne "automatic" && $item ne "manual" && $item ne "other") {
    &error("Illegal value for run_type ($item); must be automatic, manual, or other");
}

$task_id = $hashref->{"task_id"};
if ($task_id ne "kba-ccr-2013" && $task_id ne "kba-ssf-2013")  {
   &error("task_id must be `kba-ccr-2013' or 'kba-ssf-2013', not $task_id\n");
}
$line_num++;
if ($num_errors > 0) { # don't try processing run with invalid description
    exit 255;
}


# Now process remainder of submission file
while ($line = <RESULTS>) {
    $line_num++;
    chomp $line;
    next if ($line =~ /^\s*$/);

    next if ($line =~ /^\s*#/);

    my @fields = split " ", $line;
	
    if (scalar(@fields) == 11) {
       ($team,$tag,$docno,$topic,$sim,$rel_level,$mention,$date_hour,$slot,$equiv,$bytes) = @fields;
    } else {
        &error("Wrong number of fields (expecting 11)");
        exit 255;
    }

    # make sure the system-id and team-id given in the input line match
    # the description
    if ($run_id ne $tag) {
	&error("Run tag inconsistent (`$tag' and `$run_id')");
	next;
    }
    if($team_id ne $team) {
	&error("Team-id inconsistent (`$team' and `$team_id')");
	next;
    }

    # make sure topic is known
    if (!exists($topics{$topic})) {
	&error("Unknown topic '$topic'");
	next;
    }  
    
    # make sure DOCNO known 
    # check is only partial (i.e. you can construct cases where
    # check_input won't complain but in fact it is invalid)
    $docid = "";
    (undef,$docid) = split "-", $docno;
    if ($docid && $docid !~ /^[0-9a-f]{32}$/) {
	&error("Unknown document `$docno'");
	next;
    }


    if ((int($sim) != $sim) || $sim <= 0 || $sim > 1000) {
	&error("Score must be an integer in [1,1000], not $sim");
	next;
    }

    if ($rel_level != -1 && $rel_level != 0 
		&& $rel_level != 1 && $rel_level != 2) {
	&error("Relevance level must be one of {-1,0,1,2}, not $rel_level");
	next;
    }

    if ($mention != 0 && $mention != 1) {
	&error("mention must be one of {0,1}, not $mention");
	next;
    }

    if ($task_id eq "kba-ccr-2013") {
	if ($slot ne "NULL") {
	    &error("slot must be 'NULL' for CCR runs, not $slot");
	    next;
	}
	if ($equiv ne "-1") {
	    &error("equiv_class must be '-1' for CCR runs, not $equiv");
	    next;
	}
	if ($bytes ne "0-0") {
	    &error("bytes must be '0-0' for CCR runs, not $bytes");
	    next;
	}
    }
    else {
	if ($bytes !~ /^([0-9]+)-([0-9]+)$/) {
	    &error("invalid byte range ($bytes)");
	    next;
	}
	elsif ($1 > $2) {
	    &error("btye range x-y must have x<=y");
	}
    }

    $topics{$topic}++ if ($rel_level == 2);
}


# Do global checks:
#   warning if no documents retrieved for a topic
foreach $topic (keys %topics) { 
    if ($topics{$topic} == 0) {
    	print ERRLOG "WARNING: no vital retrieved for topic $topic\n"; 
    }
}


print ERRLOG "Finished processing $results_file\n";
close ERRLOG || die "Close failed for error log $errlog: $!\n";

if ($num_errors) { exit 255; }
exit 0;


# print error message, keeping track of total number of errors
sub error {
   my $msg_string = pop(@_);

    print ERRLOG 
    "run $results_file: Error on line $line_num --- $msg_string\n";

    $num_errors++;
    if ($num_errors > $MAX_ERRORS) {
        print ERRLOG "$0 of $results_file: Quit. Too many errors!\n";
        close ERRLOG ||
		die "Close failed for error log $errlog: $!\n";
	exit 255;
    }
}
