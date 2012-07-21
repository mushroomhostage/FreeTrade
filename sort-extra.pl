#!/usr/bin/perl
open(FH, "<extra.yml")||die;
scalar <FH>;
my @ids;
while(1)
{
    my $id_line = scalar <FH>;
    last if !defined($id_line);
    chomp($id_line);
    $id_line =~ s/://;
    $id_line =~ s/\s+//;
    $id_line =~ s/'//g;
    my ($id, $damage) = split /;/, $id_line;

    my $name_line = scalar <FH>;
    chomp($name_line);
    $name_line =~ s/.*?name: //g;
    my $name = $name_line;

    #print "$id,$damage,$name\n";

    push @ids, {ID=>$id, DAMAGE=>$damage, NAME=>$name};
}

my @sorted_ids = sort {
    my $order = $a->{ID} <=> $b->{ID};
    return $order if $order != 0;
    return $a->{DAMAGE} <=> $b->{DAMAGE}

    } @ids;

print "items:\n";
for my $id (@sorted_ids) {
    print "  '$id->{ID}";
    if (defined($id->{DAMAGE})) {
        print ";$id->{DAMAGE}";
    }
    print "':\n";
    print "    name: '$id->{NAME}'\n";
}
