variable vpc_cidr_block {
    default = "10.0.0.0/16"
}
variable subnet_cidr_block {
    default = "10.0.10.0/24"
}
variable availability_zone {
    default = "us-east-2a"
}
variable region {
    default = "us-east-2"
}
variable image_name {
    default = "al2023-ami-*-x86_64"
}

# Use terraform.tfvars file to set below variables
variable instance_type {}
variable env_prefix {}
variable my_ip {}
variable private_key_name {}